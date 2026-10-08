package lesson05_transactions_acid;

import config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * ==============================================================================
 * BÀI 5.2: BẢN CHẤT CỦA SAVEPOINT - HOÀN TÁC MỘT PHẦN GIAO DỊCH
 * ==============================================================================
 *
 * TẠI SAO CẦN SAVEPOINT?
 * - Trong một giao dịch phức tạp (ví dụ đặt hàng và tặng quà khuyến mãi):
 *   + Bước 1: Tạo đơn hàng chính (BẮT BUỘC).
 *   + Bước 2: Tặng mã quà tặng khuyến mãi (TÙY CHỌN, nếu hết quà thì không sao, vẫn giữ đơn hàng).
 * - Nếu không có Savepoint, một lỗi nhỏ ở Bước 2 sẽ khiến ta phải rollback() TOÀN BỘ đơn hàng.
 * - Với Savepoint:
 *   + Ta đánh dấu: `Savepoint sp = conn.setSavepoint("POINT_A");`
 *   + Nếu Bước 2 lỗi: `conn.rollback(sp);` -> Chỉ hoàn tác bước 2, bước 1 vẫn còn nguyên!
 *   + Sau đó gọi `conn.commit()` để chốt giao dịch.
 * ==============================================================================
 */
public class SavepointDemo {

    public static void run() {
        System.out.println("\n=======================================================");
        System.out.println("LAB 5.2: BẢN CHẤT SAVEPOINT (HOÀN TÁC MỘT PHẦN TRANSACTION)");
        System.out.println("=======================================================");

        DatabaseConfig.initDatabaseSchema();

        String insertUserSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            System.out.println("1. Bước 1: Thêm tài khoản VIP chính ('vip_customer')...");
            try (PreparedStatement ps1 = conn.prepareStatement(insertUserSql)) {
                ps1.setString(1, "vip_customer");
                ps1.setString(2, "secret999");
                ps1.setString(3, "VIP");
                ps1.executeUpdate();
            }

            // Đặt điểm kiểm tra (Savepoint)
            Savepoint savepoint1 = conn.setSavepoint("VIP_CREATED_POINT");
            System.out.println("   -> Đã thiết lập Savepoint: 'VIP_CREATED_POINT'.");

            System.out.println("\n2. Bước 2: Cố gắng thêm tài khoản quà tặng phụ (nhưng vi phạm/lỗi giả định)...");
            boolean giftFailed = true; // Giả lập lỗi ở bước phụ

            if (giftFailed) {
                System.out.println("   [CẢNH BÁO] Bước thêm quà tặng phụ thất bại!");
                System.out.println("   -> Thực hiện conn.rollback(savepoint1) để CHỈ hủy bước phụ, giữ nguyên tài khoản VIP!");
                conn.rollback(savepoint1);
            }

            // Commit giao dịch (bước 1 vẫn được lưu!)
            conn.commit();
            System.out.println("\n3. Đã conn.commit() thành công phần nghiệp vụ chính!");

            // Kiểm tra xem 'vip_customer' có tồn tại trong DB không
            try (PreparedStatement psCheck = conn.prepareStatement("SELECT username, role FROM users WHERE username = 'vip_customer'");
                 ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("   -> XÁC NHẬN: Tài khoản '%s' với quyền '%s' ĐÃ ĐƯỢC LƯU AN TOÀN!%n",
                            rs.getString("username"), rs.getString("role"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }
}
