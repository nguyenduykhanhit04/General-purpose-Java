package lesson07_diy_connection_pool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ==============================================================================
 * BÀI 7.1: BẢN CHẤT CONNECTION POOL (TỰ TAY VIẾT 1 MINI HIKARICP)
 * ==============================================================================
 *
 * CÂU HỎI LỚN NHẤT CỦA LẬP TRÌNH VIÊN VỀ CONNECTION POOL:
 * "Trong code Spring Boot hay JDBC, ta luôn viết:
 *     try (Connection conn = dataSource.getConnection()) { ... }
 *  Khi khối try kết thúc, hàm conn.close() BẮT BUỘC ĐƯỢC GỌI.
 *  Nếu conn.close() ĐÓNG THẬT SỰ SOCKET MẠNG, thì Connection Pool sinh ra để làm gì?"
 *
 * BÍ MẬT BỊ BẬT MÍ:
 * 1. Connection Pool KHÔNG BAO GIỜ trả về Connection thật của Driver!
 * 2. Thay vào đó, nó bọc Connection thật bên trong một "DYNAMIC PROXY" (hoặc Wrapper Object).
 * 3. Khi lập trình viên gọi `conn.close()`:
 *    - Dynamic Proxy (InvocationHandler) sẽ CHẶN (intercept) phương thức "close" lại!
 *    - Thay vì gọi lệnh đóng socket thật, nó âm thầm TRẢ CONNECTION VỀ LẠI HÀNG ĐỢI (Queue) của Pool!
 * 4. Tất cả các phương thức khác (prepareStatement, commit, rollback...) thì vẫn ủy quyền (delegate)
 *    chạy trên Connection thật!
 *
 * Hãy xem mã nguồn dưới đây: Chỉ khoảng 80 dòng code để tái hiện bản chất cốt lõi của HikariCP!
 * ==============================================================================
 */
public class SimpleConnectionPool {

    private final String url;
    private final String user;
    private final String password;
    private final int poolSize;
    private final BlockingQueue<Connection> pool;
    private final AtomicInteger createdCount = new AtomicInteger(0);

    public SimpleConnectionPool(String url, String user, String password, int poolSize) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.poolSize = poolSize;
        this.pool = new ArrayBlockingQueue<>(poolSize);

        // Khởi tạo sẵn một số lượng kết nối thật (Eager Initialization)
        for (int i = 0; i < poolSize; i++) {
            try {
                Connection realConn = DriverManager.getConnection(url, user, password);
                pool.offer(realConn);
                createdCount.incrementAndGet();
            } catch (SQLException e) {
                throw new RuntimeException("Lỗi khởi tạo connection pool: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Lấy một kết nối từ Pool. Kết nối này là một DYNAMIC PROXY!
     */
    public Connection getConnection() throws SQLException {
        try {
            // Chờ tối đa 5 giây nếu tất cả kết nối trong pool đang bận
            Connection realConnection = pool.poll(5, TimeUnit.SECONDS);
            if (realConnection == null) {
                throw new SQLException("Connection Pool Timeout: Hết kết nối khả dụng!");
            }

            // TẠO DYNAMIC PROXY ĐỂ ĐÁNH CHẶN HÀM close()
            return (Connection) Proxy.newProxyInstance(
                    SimpleConnectionPool.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            // NẾU LÀ GỌI HÀM CLOSE()
                            if ("close".equals(method.getName())) {
                                // KHÔNG ĐÓNG KẾT NỐI THẬT! Đưa kết nối thật trở lại hàng đợi Pool!
                                pool.offer(realConnection);
                                return null;
                            }

                            // CÁC HÀM KHÁC (prepareStatement, createStatement...) -> Chạy trên kết nối thật
                            return method.invoke(realConnection, args);
                        }
                    }
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Bị ngắt quãng khi chờ kết nối", e);
        }
    }

    public int getAvailableConnections() {
        return pool.size();
    }

    public int getTotalCreatedConnections() {
        return createdCount.get();
    }

    /**
     * Khi ứng dụng tắt hoàn toàn, mới đóng thật sự toàn bộ socket
     */
    public void shutdown() {
        while (!pool.isEmpty()) {
            Connection conn = pool.poll();
            if (conn != null) {
                try {
                    conn.close(); // Đóng thật
                } catch (SQLException ignored) {}
            }
        }
    }
}
