package lesson04_resultset_cursor;

import config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ==============================================================================
 * BÀI 4.1: BẢN CHẤT RESULTSET, CON TRỎ (CURSOR) VÀ FETCH SIZE
 * ==============================================================================
 *
 * SAI LẦM PHỔ BIẾN:
 * - Nhiều lập trình viên nghĩ rằng khi gọi `ResultSet rs = ps.executeQuery();`,
 *   Java đã tải toàn bộ 100,000 hoặc 1,000,000 bản ghi về nạp đầy bộ nhớ RAM (Heap).
 *
 * SỰ THẬT VỀ BẢN CHẤT:
 * 1. ResultSet KHÔNG PHẢI là một List lưu toàn bộ dữ liệu trong RAM!
 * 2. ResultSet thực chất là một CON TRỎ (CURSOR) trỏ vào bộ đệm dữ liệu (Buffer)
 *    nhận qua luồng mạng (Socket) từ Database Server.
 * 3. Ban đầu, con trỏ đứng ở vị trí "Before First" (trước dòng đầu tiên).
 * 4. Khi gọi `rs.next()`:
 *    - Con trỏ dịch chuyển xuống 1 dòng.
 *    - Trả về `true` nếu còn dòng dữ liệu, `false` nếu đã đến cuối ("After Last").
 * 5. Cơ chế Fetch Size (`setFetchSize(int rows)`):
 *    - Giả sử bảng có 1,000,000 dòng.
 *    - Nếu fetchSize = 50: Driver sẽ chỉ kéo 50 dòng qua mạng về đệm của máy Client.
 *      Khi gọi `rs.next()` đến dòng thứ 51, Driver mới âm thầm gửi tín hiệu qua Socket
 *      để kéo tiếp 50 dòng tiếp theo về!
 *    - Giúp ứng dụng xử lý hàng triệu bản ghi (Stream processing) mà KHÔNG BAO GIỜ bị tràn bộ nhớ (OutOfMemoryError)!
 * ==============================================================================
 */
public class CursorAndFetchSizeDemo {

    public static void run() {
        System.out.println("\n=======================================================");
        System.out.println("LAB 4.1: BẢN CHẤT RESULTSET, CON TRỎ (CURSOR) & FETCH SIZE");
        System.out.println("=======================================================");

        DatabaseConfig.initDatabaseSchema();

        // Tạo 20 sinh viên mẫu để kiểm tra
        seedData(20);

        String sql = "SELECT id, name, gpa FROM students ORDER BY id";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Cấu hình Fetch Size: Yêu cầu Driver kéo 5 dòng mỗi đợt qua mạng
            ps.setFetchSize(5);

            System.out.println("1. Đã đặt FetchSize = 5. Bắt đầu duyệt ResultSet với con trỏ (Cursor)...");

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    // rs.getString, rs.getDouble đọc dữ liệu TẠI DÒNG HIỆN TẠI mà con trỏ đang đứng
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    double gpa = rs.getDouble("gpa");

                    if (count <= 5 || count >= 18) {
                        System.out.printf("   [Dòng %02d - Cursor trỏ tới] ID: %s | Tên: %s | GPA: %.1f%n",
                                count, id, name, gpa);
                    } else if (count == 6) {
                        System.out.println("   [... các dòng từ 6 đến 17 đang được đọc tiếp tục ...]");
                    }
                }
                System.out.printf("-> Duyệt xong %d dòng. Con trỏ đã chuyển sang trạng thái After Last (rs.next() trả về false).%n", count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void seedData(int count) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "MERGE INTO students (id, name, age, email, gender, gpa) VALUES (?, ?, ?, ?, ?, ?)")) {
            for (int i = 1; i <= count; i++) {
                ps.setString(1, String.format("SV%03d", i));
                ps.setString(2, "Sinh viên " + i);
                ps.setInt(3, 20);
                ps.setString(4, "student" + i + "@university.edu.vn");
                ps.setString(5, i % 2 == 0 ? "MALE" : "FEMALE");
                ps.setDouble(6, 7.0 + (i % 30) / 10.0);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
