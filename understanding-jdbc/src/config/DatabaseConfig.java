package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ==============================================================================
 * BẢN CHẤT CỦA DATABASE CONFIG & KẾT NỐI JDBC
 * ==============================================================================
 * 1. JDBC URL CẤU TRÚC NHƯ THẾ NÀO?
 *    Cú pháp chuẩn: jdbc:<subprotocol>:<subname>
 *    - jdbc        : Giao thức chính (luôn bắt đầu bằng jdbc:).
 *    - <subprotocol>: Tên hệ quản trị CSDL hoặc loại driver (vd: h2, mysql, postgresql, sqlite).
 *    - <subname>   : Chuỗi định danh chi tiết (host, port, tên db, chế độ in-memory...).
 *
 * 2. TẠI SAO CHỌN H2 IN-MEMORY CHO DỰ ÁN HỌC BẢN CHẤT?
 *    - 100% viết bằng Java nguyên bản, chạy nhúng (embedded/in-memory) ngay trong tiến trình JVM.
 *    - Không cần cài đặt server MySQL/Postgres, không lo xung đột port, chạy được trên mọi máy tính.
 *    - Tuy nhiên: Code JDBC viết cho H2 giống 100% code viết cho MySQL hay Oracle!
 *      Đó chính là SỨC MẠNH CỦA SỰ TRỪU TƯỢNG HÓA TRONG JDBC!
 * ==============================================================================
 */
public class DatabaseConfig {

    // URL kết nối H2 ở chế độ In-Memory:
    // DB_CLOSE_DELAY=-1 giữ cho database sống suốt vòng đời của JVM cho đến khi chương trình tắt.
    public static final String H2_IN_MEMORY_URL = "jdbc:h2:mem:mastery_db;DB_CLOSE_DELAY=-1";
    public static final String H2_USER = "sa";
    public static final String H2_PASSWORD = "";

    // Cấu hình mẫu cho MySQL nếu người dùng muốn chuyển đổi kiểm tra:
    public static final String MYSQL_URL = "jdbc:mysql://localhost:3306/mastery_db?useSSL=false&allowPublicKeyRetrieval=true";
    public static final String MYSQL_USER = "root";
    public static final String MYSQL_PASSWORD = "root";

    /**
     * Lấy một kết nối thô (raw connection) mới tới H2 Database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(H2_IN_MEMORY_URL, H2_USER, H2_PASSWORD);
    }

    /**
     * Khởi tạo các bảng dữ liệu mẫu phục vụ cho toàn bộ các bài thực hành.
     */
    public static void initDatabaseSchema() {
        String createStudentsTable = """
            CREATE TABLE IF NOT EXISTS students (
                id VARCHAR(20) PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                age INT NOT NULL,
                email VARCHAR(100) NOT NULL,
                gender VARCHAR(10) NOT NULL,
                gpa DOUBLE NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String createAccountsTable = """
            CREATE TABLE IF NOT EXISTS bank_accounts (
                account_number VARCHAR(20) PRIMARY KEY,
                owner_name VARCHAR(100) NOT NULL,
                balance DOUBLE NOT NULL
            );
        """;

        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) NOT NULL,
                password VARCHAR(100) NOT NULL,
                role VARCHAR(20) NOT NULL
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createStudentsTable);
            stmt.execute(createAccountsTable);
            stmt.execute(createUsersTable);

            // Xóa dữ liệu cũ nếu có và thêm dữ liệu khởi tạo
            stmt.execute("DELETE FROM users");
            stmt.execute("INSERT INTO users (username, password, role) VALUES ('admin', 'admin@123', 'ADMIN')");
            stmt.execute("INSERT INTO users (username, password, role) VALUES ('john', 'pass456', 'USER')");

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khởi tạo database schema: " + e.getMessage(), e);
        }
    }
}
