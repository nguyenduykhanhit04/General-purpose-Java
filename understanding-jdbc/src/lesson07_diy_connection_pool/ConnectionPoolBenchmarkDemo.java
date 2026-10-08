package lesson07_diy_connection_pool;

import config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ==============================================================================
 * BÀI 7.2: BENCHMARK CONNECTION POOL TRONG MÔI TRƯỜNG ĐA LUỒNG (CONCURRENCY)
 * ==============================================================================
 *
 * MÔ PHỎNG MỘT WEB SERVER (VD: SPRING BOOT / TOMCAT):
 * - Giả sử có 100 lượt người dùng (Requests) đồng thời truy vấn cơ sở dữ liệu.
 * - Ta so sánh:
 *   + Cách 1: Mỗi request tạo 1 Raw Connection mới qua DriverManager rồi đóng lại (100 lần bắt tay mạng).
 *   + Cách 2: Sử dụng SimpleConnectionPool với kích thước cố định chỉ 5 connections!
 * ==============================================================================
 */
public class ConnectionPoolBenchmarkDemo {

    private static final int TOTAL_REQUESTS = 100;
    private static final int THREAD_POOL_SIZE = 10;

    public static void run() {
        System.out.println("\n=======================================================");
        System.out.println("LAB 7.2: BENCHMARK CONNECTION POOL VỚI " + TOTAL_REQUESTS + " REQUESTS ĐỒNG THỜI");
        System.out.println("=======================================================");

        DatabaseConfig.initDatabaseSchema();

        // 1. Chạy với Raw DriverManager Connection
        long rawTime = benchmarkRawConnections();

        // 2. Chạy với DIY SimpleConnectionPool
        SimpleConnectionPool pool = new SimpleConnectionPool(
                DatabaseConfig.H2_IN_MEMORY_URL,
                DatabaseConfig.H2_USER,
                DatabaseConfig.H2_PASSWORD,
                5 // Chỉ cần 5 kết nối phục vụ 100 requests!
        );
        long poolTime = benchmarkWithPool(pool);

        System.out.println("\n================== KẾT QUẢ BENCHMARK ==================");
        System.out.printf("1. Raw Connections (Tạo mới 100 lần) : %,d ms%n", rawTime);
        System.out.printf("2. Connection Pool (Tái sử dụng 5 kết nối) : %,d ms%n", poolTime);
        System.out.printf("-> Tổng số kết nối vật lý Pool đã tạo: %d kết nối!%n", pool.getTotalCreatedConnections());
        System.out.println("-> Sau khi hoàn tất, số kết nối sẵn sàng trong Pool: " + pool.getAvailableConnections());
        System.out.println("=======================================================");

        pool.shutdown();
    }

    private static long benchmarkRawConnections() {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch latch = new CountDownLatch(TOTAL_REQUESTS);

        long start = System.currentTimeMillis();
        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            executor.submit(() -> {
                try {
                    // Mở kết nối mới từ DriverManager
                    try (Connection conn = DriverManager.getConnection(
                            DatabaseConfig.H2_IN_MEMORY_URL,
                            DatabaseConfig.H2_USER,
                            DatabaseConfig.H2_PASSWORD);
                         PreparedStatement ps = conn.prepareStatement("SELECT 1");
                         ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {
                            rs.getInt(1);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException ignored) {}
        executor.shutdown();

        return System.currentTimeMillis() - start;
    }

    private static long benchmarkWithPool(SimpleConnectionPool pool) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch latch = new CountDownLatch(TOTAL_REQUESTS);

        long start = System.currentTimeMillis();
        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            executor.submit(() -> {
                try {
                    // Lấy kết nối Proxy từ Pool
                    try (Connection conn = pool.getConnection();
                         PreparedStatement ps = conn.prepareStatement("SELECT 1");
                         ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {
                            rs.getInt(1);
                        }
                        // Khi kết thúc try-with-resources: conn.close() được gọi!
                        // Nhưng nhờ Dynamic Proxy, nó KHÔNG đóng socket mà trả kết nối về pool!
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException ignored) {}
        executor.shutdown();

        return System.currentTimeMillis() - start;
    }
}
