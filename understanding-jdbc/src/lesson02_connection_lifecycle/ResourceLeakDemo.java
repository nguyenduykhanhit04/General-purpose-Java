package lesson02_connection_lifecycle;

import config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ==============================================================================
 * BÀI 2.2: NGUY CƠ RESOURCE LEAK & TIẾN HÓA CỦA QUẢN LÝ TÀI NGUYÊN TRONG JDBC
 * ==============================================================================
 *
 * TẠI SAO PHẢI CLOSE()?
 * 1. Connection: Giữ kết nối mạng (Socket TCP / File descriptor OS). Nếu không close(),
 *    hệ điều hành sẽ hết file descriptor -> Server treo hoặc báo lỗi "Too many open files".
 * 2. Statement / PreparedStatement: Phía Database Server giữ con trỏ câu lệnh, bộ nhớ đệm
 *    kế hoạch thực thi (cursor handle). Không close() gây rò rỉ bộ nhớ phía Database Server.
 * 3. ResultSet: Giữ con trỏ hàng (row cursor) và bộ đệm mạng từ Database về Client.
 *
 * THỨ TỰ ĐÓNG CHUẨN:
 *    Đóng theo thứ tự ngược lại lúc mở: ResultSet -> Statement -> Connection.
 *
 * TIẾN HÓA:
 * - Java 6 trở về trước: Cơn ác mộng "Finally pyramid of doom" (lồng hàng loạt try-catch-finally).
 * - Java 7 trở đi: AutoCloseable + try-with-resources -> Tự động đóng tài nguyên an toàn 100%,
 *   kể cả khi xảy ra Exception!
 * ==============================================================================
 */
public class ResourceLeakDemo {

    public static void run() {
        System.out.println("\n=======================================================");
        System.out.println("LAB 2.2: QUẢN LÝ TÀI NGUYÊN (RESOURCE LEAK & TRY-WITH-RESOURCES)");
        System.out.println("=======================================================");

        System.out.println("1. Cách cũ (Java 6 trở về trước): Finally Pyramid of Doom:");
        demoLegacyWay();

        System.out.println("\n2. Cách chuẩn mực (Java 7+): Try-with-resources với AutoCloseable:");
        demoModernWay();

        System.out.println("\nBẢN CHẤT:");
        System.out.println("   - Cả 3 interface: Connection, Statement, ResultSet đều kế thừa từ java.lang.AutoCloseable.");
        System.out.println("   - Trình biên dịch (javac) sẽ tự động sinh mã bytecode đóng tài nguyên trong khối finally ngầm.");
    }

    private static void demoLegacyWay() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement("SELECT COUNT(*) FROM users");
            rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("   -> Kết quả: " + rs.getInt(1) + " users");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Cơn ác mộng: Phải kiểm tra null và bọc try-catch riêng cho từng cái!
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    /* nuốt lỗi hoặc log */
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    /* nuốt lỗi */
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    /* nuốt lỗi */
                }
            }
            System.out.println("   -> Đã đóng thủ công theo kiểu cũ an toàn (nhưng code dài dòng, dễ quên).");
        }
    }

    private static void demoModernWay() {
        // Cú pháp gọn gàng, tự động đóng ngược thứ tự khai báo (rs -> ps -> conn)
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                System.out.println("   -> Kết quả: " + rs.getInt(1) + " users");
            }
            System.out.println("   -> Khối try kết thúc: Tự động đóng rs, ps, conn ngay tức thì.");

        } catch (SQLException e) {
            System.err.println("   -> Lỗi: " + e.getMessage());
        }
    }
}
