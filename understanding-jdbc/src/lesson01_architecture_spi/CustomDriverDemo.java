package lesson01_architecture_spi;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * ==============================================================================
 * BÀI 1.1: BẢN CHẤT JDBC LÀ GÌ? TỰ TAY VIẾT 1 JDBC DRIVER ĐỂ HIỂU HỢP ĐỒNG (CONTRACT)
 * ==============================================================================
 * 
 * BẢN CHẤT CỐT LÕI:
 * - JDBC (Java Database Connectivity) KHÔNG PHẢI là một thư viện kết nối cơ sở dữ liệu cụ thể.
 * - JDBC thực chất là một TẬP HỢP CÁC INTERFACES nằm trong package java.sql và javax.sql.
 * - Oracle, MySQL, Microsoft, Postgres, SQLite... KHÔNG SỬA MÃ NGUỒN CỦA JAVA để Java hiểu DB của họ.
 * - Thay vào đó, Java đưa ra hợp đồng (Interface) -> Các hãng CSDL phải tự viết các class thực thi
 *   (Implementation) các Interface đó. File JAR chứa các class thực thi đó được gọi là "JDBC Driver".
 *
 * Trong bài này, chúng ta sẽ tự viết một "ToyJdbcDriver" thực thi interface java.sql.Driver!
 * ==============================================================================
 */
public class CustomDriverDemo {

    /**
     * Một Driver tự chế mô phỏng. Bất kỳ JDBC Driver nào trên thế giới (kể cả com.mysql.cj.jdbc.Driver)
     * cũng đều phải implement interface java.sql.Driver giống như class dưới đây!
     */
    public static class ToyCustomDriver implements Driver {
        private static final String PREFIX = "jdbc:toydb://";

        static {
            try {
                // Tự đăng ký chính mình vào DriverManager khi class được load
                DriverManager.registerDriver(new ToyCustomDriver());
                System.out.println("   -> [ToyCustomDriver] Đã tự đăng ký vào java.sql.DriverManager thành công!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            if (!acceptsURL(url)) {
                // Nếu URL không thuộc quyền quản lý của Driver này, trả về null để DriverManager hỏi driver tiếp theo!
                return null;
            }
            System.out.println("   -> [ToyCustomDriver.connect] Nhận diện được URL: " + url);
            System.out.println("   -> [ToyCustomDriver.connect] Khởi tạo đối tượng Connection mô phỏng...");
            // Trong thực tế, tại đây Driver sẽ mở Socket TCP tới Database Server (vd port 3306)
            // Ở đây ta tạo một Dynamic Proxy của interface java.sql.Connection:
            return (Connection) java.lang.reflect.Proxy.newProxyInstance(
                    CustomDriverDemo.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    (proxy, method, methodArgs) -> {
                        System.out.println("      [ToyConnection] Đã kích hoạt hàm: " + method.getName());
                        if ("toString".equals(method.getName())) {
                            return "ToyConnectionProxy@" + Integer.toHexString(System.identityHashCode(proxy));
                        }
                        return null;
                    }
            );
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            // Kiểm tra xem URL có bắt đầu bằng tiền tố quy định của hãng hay không
            return url != null && url.startsWith(PREFIX);
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
            return new DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion() {
            return 1;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public boolean jdbcCompliant() {
            return false;
        }

        @Override
        public Logger getParentLogger() {
            return Logger.getLogger(ToyCustomDriver.class.getName());
        }
    }

    public static void run() {
        System.out.println("\n=======================================================");
        System.out.println("LAB 1.1: BẢN CHẤT JDBC INTERFACE VÀ CUSTOM DRIVER");
        System.out.println("=======================================================");

        try {
            System.out.println("1. Tải ToyCustomDriver vào bộ nhớ JVM (kích hoạt khối static)...");
            Class.forName(ToyCustomDriver.class.getName());

            System.out.println("\n2. Thử gọi DriverManager.getConnection với URL: jdbc:toydb://localhost:9999/mydb");
            Connection conn = DriverManager.getConnection("jdbc:toydb://localhost:9999/mydb");
            System.out.println("   -> [THÀNH CÔNG] DriverManager đã trả về Connection: " + conn);
            conn.close(); // Thử gọi close trên proxy connection!

            System.out.println("\n3. Thử gọi DriverManager.getConnection với URL không hỗ trợ: jdbc:unsupported://test");
            try {
                DriverManager.getConnection("jdbc:unsupported://test");
            } catch (SQLException ex) {
                System.out.println("   -> DriverManager báo lỗi như mong đợi: " + ex.getMessage());
                System.out.println("   -> GIẢI THÍCH: DriverManager đã lặp qua TẤT CẢ các Driver đã đăng ký.");
                System.out.println("      Mỗi Driver đều trả về acceptsURL() = false, do đó không có driver nào nhận!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
