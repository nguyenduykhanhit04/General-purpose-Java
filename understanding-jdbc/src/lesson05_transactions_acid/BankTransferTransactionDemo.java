package lesson05_transactions_acid;

import config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ==============================================================================
 * BÀI 5.1: BẢN CHẤT GIAO DỊCH (TRANSACTION) & TÍNH TOÀN VẸN ACID TRONG JDBC
 * ==============================================================================
 *
 * MẶC ĐỊNH CỦA JDBC:
 * - Khi mở Connection, `conn.getAutoCommit()` mặc định là TRUE!
 * - Nghĩa là: MỖI CÂU LỆNH executeUpdate() đều được tự động COMMIT NGAY LẬP TỨC thành một Transaction riêng biệt.
 *
 * TAI HỌA TRONG NGHIỆP VỤ NGÂN HÀNG (CHUYỂN TIỀN):
 * - Nghiệp vụ chuyển tiền gồm 2 thao tác:
 *   (1) Trừ tiền tài khoản A.
 *   (2) Cộng tiền tài khoản B.
 * - Nếu dùng autoCommit = true:
 *   Sau khi bước (1) chạy xong, server bị cúp điện hoặc ném ngoại lệ -> Bước (2) không chạy!
 *   => Tiền của A đã mất, B không nhận được gì -> Mất tiền oan! Vi phạm tính Atomicity (Nguyên tử)!
 *
 * GIẢI PHÁP:
 *   - Bắt đầu transaction: `conn.setAutoCommit(false);`
 *   - Thực thi tất cả các bước.
 *   - Thành công: `conn.commit();`
 *   - Thất bại / Exception: `conn.rollback();` trong khối catch.
 * ==============================================================================
 */
public class BankTransferTransactionDemo {

    public static void run() {
        System.out.println("\n=======================================================");
        System.out.println("LAB 5.1: BẢN CHẤT GIAO DỊCH (TRANSACTION) & ACID");
        System.out.println("=======================================================");

        DatabaseConfig.initDatabaseSchema();

        // Khởi tạo 2 tài khoản ngân hàng
        resetAccounts();
        printAccountBalances("TRẠNG THÁI BAN ĐẦU");

        // Kịch bản 1: Không dùng Transaction (AutoCommit = true) -> Xảy ra sự cố -> Mất tiền!
        System.out.println("\n--- [KỊCH BẢN 1] CHUYỂN TIỀN VỚI AUTO-COMMIT = TRUE (MẶC ĐỊNH - NGUY HIỂM) ---");
        transferWithoutTransaction("ACC_A", "ACC_B", 300_000, true);
        printAccountBalances("KẾT QUẢ KỊCH BẢN 1 (Mất tiền do trừ A mà chưa cộng B!)");

        // Khởi tạo lại tài khoản
        resetAccounts();

        // Kịch bản 2: Dùng Transaction (AutoCommit = false) -> Xảy ra sự cố -> Rollback an toàn!
        System.out.println("\n--- [KỊCH BẢN 2] CHUYỂN TIỀN VỚI AUTO-COMMIT = FALSE & ROLLBACK AN TOÀN ---");
        transferWithTransaction("ACC_A", "ACC_B", 300_000, true);
        printAccountBalances("KẾT QUẢ KỊCH BẢN 2 (Rollback thành công, số dư nguyên vẹn!)");
    }

    private static void resetAccounts() {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement psDel = conn.prepareStatement("DELETE FROM bank_accounts");
             PreparedStatement psIns = conn.prepareStatement(
                     "INSERT INTO bank_accounts (account_number, owner_name, balance) VALUES (?, ?, ?)")) {

            psDel.executeUpdate();

            psIns.setString(1, "ACC_A");
            psIns.setString(2, "Nguyen Van A");
            psIns.setDouble(3, 1_000_000);
            psIns.executeUpdate();

            psIns.setString(1, "ACC_B");
            psIns.setString(2, "Tran Thi B");
            psIns.setDouble(3, 500_000);
            psIns.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void printAccountBalances(String label) {
        System.out.println("-> " + label + ":");
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT account_number, owner_name, balance FROM bank_accounts");
             ResultSet rs = ps.executeQuery()) {

            double total = 0;
            while (rs.next()) {
                String acc = rs.getString("account_number");
                String name = rs.getString("owner_name");
                double bal = rs.getDouble("balance");
                total += bal;
                System.out.printf("   [Tài khoản %s (%s)]: %,.0f VND%n", acc, name, bal);
            }
            System.out.printf("   => TỔNG TIỀN CẢ HỆ THỐNG: %,.0f VND%n", total);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void transferWithoutTransaction(String fromAcc, String toAcc, double amount, boolean simulateCrash) {
        String deductSql = "UPDATE bank_accounts SET balance = balance - ? WHERE account_number = ?";
        String creditSql = "UPDATE bank_accounts SET balance = balance + ? WHERE account_number = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            System.out.println("   [AutoCommit = " + conn.getAutoCommit() + "]");

            // Bước 1: Trừ tiền A
            try (PreparedStatement psDeduct = conn.prepareStatement(deductSql)) {
                psDeduct.setDouble(1, amount);
                psDeduct.setString(2, fromAcc);
                psDeduct.executeUpdate();
                System.out.printf("   -> Đã trừ %,.0f VND từ %s (Đã tự động commit xuống DB!)%n", amount, fromAcc);
            }

            // Giả lập sự cố mất điện / đứt cáp / ngoại lệ phần mềm
            if (simulateCrash) {
                System.out.println("   [SỰ CỐ!] Server bị sập bất ngờ trước khi cộng tiền cho B...");
                throw new RuntimeException("Lỗi hệ thống: Mất kết nối đường truyền đột ngột!");
            }

            // Bước 2: Cộng tiền B (sẽ không bao giờ tới được đây nếu crash)
            try (PreparedStatement psCredit = conn.prepareStatement(creditSql)) {
                psCredit.setDouble(1, amount);
                psCredit.setString(2, toAcc);
                psCredit.executeUpdate();
                System.out.printf("   -> Đã cộng %,.0f VND cho %s%n", amount, toAcc);
            }

        } catch (Exception e) {
            System.err.println("   -> Bắt được ngoại lệ: " + e.getMessage());
        }
    }

    private static void transferWithTransaction(String fromAcc, String toAcc, double amount, boolean simulateCrash) {
        String deductSql = "UPDATE bank_accounts SET balance = balance - ? WHERE account_number = ?";
        String creditSql = "UPDATE bank_accounts SET balance = balance + ? WHERE account_number = ?";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();

            // TẮT CHẾ ĐỘ TỰ ĐỘNG COMMIT -> Bắt đầu Transaction tường minh!
            conn.setAutoCommit(false);
            System.out.println("   -> Đã đặt conn.setAutoCommit(false). Giao dịch bắt đầu...");

            // Bước 1: Trừ tiền A
            try (PreparedStatement psDeduct = conn.prepareStatement(deductSql)) {
                psDeduct.setDouble(1, amount);
                psDeduct.setString(2, fromAcc);
                psDeduct.executeUpdate();
                System.out.printf("   -> Đã trừ %,.0f VND từ %s (Tạm thời trong Transaction)%n", amount, fromAcc);
            }

            // Giả lập sự cố
            if (simulateCrash) {
                System.out.println("   [SỰ CỐ!] Server bị lỗi logic đột ngột...");
                throw new RuntimeException("Lỗi chuyển tiền!");
            }

            // Bước 2: Cộng tiền B
            try (PreparedStatement psCredit = conn.prepareStatement(creditSql)) {
                psCredit.setDouble(1, amount);
                psCredit.setString(2, toAcc);
                psCredit.executeUpdate();
                System.out.printf("   -> Đã cộng %,.0f VND cho %s%n", amount, toAcc);
            }

            // Nếu mọi thứ êm đẹp -> COMMIT
            conn.commit();
            System.out.println("   -> Đã gọi conn.commit(): Tất cả thay đổi được ghi vĩnh viễn vào DB!");

        } catch (Exception e) {
            System.err.println("   -> Bắt được lỗi: " + e.getMessage());
            if (conn != null) {
                try {
                    // HOÀN TÁC TOÀN BỘ GIAO DỊCH
                    System.out.println("   -> Đang thực hiện conn.rollback()...");
                    conn.rollback();
                    System.out.println("   -> ROLLBACK THÀNH CÔNG! Trạng thái dữ liệu được khôi phục như trước khi giao dịch bắt đầu.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Trả lại trạng thái mặc định trước khi đóng
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }
}
