package lesson03_statement_vs_prepared;

import config.DatabaseConfig;

import java.sql.*;

/**
 * ==============================================================================
 * BÀI 3.2: BẢN CHẤT TIỀN BIÊN DỊCH (PRE-COMPILATION) & LẤY AUTO-INCREMENT KEY
 * ==============================================================================
 *
 * 1. CÁC GIAI ĐOẠN XỬ LÝ TRUY VẤN PHÍA DATABASE SERVER:
 *    - Bước 1 (Parsing): Kiểm tra cú pháp SQL, phân tích từ vựng.
 *    - Bước 2 (Semantic Analysis): Kiểm tra bảng, cột có tồn tại không, quyền truy cập của user.
 *    - Bước 3 (Optimization): Bộ tối ưu hóa (Cost-based Optimizer) tính toán chi phí (dùng index nào, join bảng nào trước)
 *      để sinh ra Kế hoạch thực thi (Execution Plan). Bước này TỐN CPU NHẤT!
 *    - Bước 4 (Execution): Chạy kế hoạch và trả về kết quả.
 *
 * 2. ƯU THẾ VƯỢT TRỘI CỦA PREPAREDSTATEMENT VỀ HIỆU NĂNG:
 *    - Với Statement thông thường: Database phải làm lại cả 4 bước cho MỖI LẦN gọi.
 *    - Với PreparedStatement: Database chỉ làm Bước 1, 2, 3 MỘT LẦN DUY NHẤT và lưu kết quả vào bộ nhớ đệm (Plan Cache).
 *      Các lần gọi tiếp theo chỉ việc nạp tham số mới vào và chạy ngay Bước 4!
 *
 * 3. KỸ THUẬT LẤY AUTO-INCREMENT KEY (Statement.RETURN_GENERATED_KEYS):
 *    - Khi thêm một dòng có khóa chính tự tăng (AUTO_INCREMENT), làm sao biết ID vừa sinh ra là bao nhiêu?
 *    - Đây chính là cách Hibernate / JPA lấy ID của entity sau khi persist!
 * ==============================================================================
 */
public class PreparedStatementDeepDiveDemo {

    public static void run() {
        System.out.println("\n=======================================================");
        System.out.println("LAB 3.2: TIỀN BIÊN DỊCH & LẤY AUTO-INCREMENT KEY");
        System.out.println("=======================================================");

        DatabaseConfig.initDatabaseSchema();

        String insertSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        // Yêu cầu JDBC Driver trả về Generated Keys (ID tự sinh)
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            System.out.println("1. Thực thi PreparedStatement và lấy khóa chính tự sinh:");

            // Bản ghi 1
            ps.setString(1, "alice");
            ps.setString(2, "alice_secret");
            ps.setString(3, "MODERATOR");
            int rowsAffected = ps.executeUpdate();

            // Lấy ResultSet chứa các generated keys
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long generatedId = generatedKeys.getLong(1);
                    System.out.printf("   -> Chèn user 'alice' thành công (Rows affected: %d), ID vừa sinh: %d%n",
                            rowsAffected, generatedId);
                }
            }

            // Bản ghi 2: Tái sử dụng cùng một PreparedStatement đã biên dịch!
            ps.setString(1, "bob");
            ps.setString(2, "bob_password");
            ps.setString(3, "USER");
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long generatedId = generatedKeys.getLong(1);
                    System.out.printf("   -> Chèn user 'bob' thành công, ID vừa sinh: %d (Tái sử dụng Plan Cache!)%n", generatedId);
                }
            }

            System.out.println("\n2. Cách xử lý giá trị NULL an toàn trong PreparedStatement:");
            System.out.println("   - Không thể gán ps.setInt(1, null) vì int là primitive!");
            System.out.println("   - Giải pháp: dùng ps.setNull(index, Types.INTEGER) hoặc ps.setObject(index, null).");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
