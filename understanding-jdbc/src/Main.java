import config.DatabaseConfig;
import lesson01_architecture_spi.CustomDriverDemo;
import lesson01_architecture_spi.DriverManagerDeepDiveDemo;
import lesson02_connection_lifecycle.RawConnectionCostDemo;
import lesson02_connection_lifecycle.ResourceLeakDemo;
import lesson03_statement_vs_prepared.PreparedStatementDeepDiveDemo;
import lesson03_statement_vs_prepared.SqlInjectionExploitDemo;
import lesson04_resultset_cursor.CursorAndFetchSizeDemo;
import lesson04_resultset_cursor.ResultSetMetadataDemo;
import lesson05_transactions_acid.BankTransferTransactionDemo;
import lesson05_transactions_acid.SavepointDemo;
import lesson06_batch_processing.BatchInsertBenchmarkDemo;
import lesson07_diy_connection_pool.ConnectionPoolBenchmarkDemo;
import lesson08_mini_orm_and_dao.MiniOrmDemo;

import java.util.Scanner;

/**
 * ==============================================================================
 * DỰ ÁN THỰC NGHIỆM: HIỂU RÕ BẢN CHẤT CỐT LÕI CỦA JDBC (JDBC DEEP DIVE)
 * ==============================================================================
 *
 * MENU TRỰC QUAN ĐỂ CHẠY VÀ QUAN SÁT TỪNG NGUYÊN LÝ HOẠT ĐỘNG.
 * ==============================================================================
 */
public class Main {

    public static void main(String[] args) {
        // Tự động khởi tạo Schema ban đầu
        DatabaseConfig.initDatabaseSchema();

        // Nếu có tham số dòng lệnh (vd: java Main 1, java Main 8, java Main all)
        if (args.length > 0) {
            String arg = args[0].trim();
            if ("all".equalsIgnoreCase(arg) || "10".equals(arg)) {
                runAllLabs();
            } else {
                executeChoice(arg);
            }
            return;
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            printMenu();
            System.out.print("👉 Nhập lựa chọn của bạn (0-10): ");
            String input = scanner.nextLine().trim();

            System.out.println();
            if ("0".equals(input)) {
                System.out.println("Tạm biệt! Chúc bạn làm chủ hoàn toàn bản chất của JDBC.");
                return;
            }

            executeChoice(input);

            System.out.println("\n-------------------------------------------------------");
            System.out.println("Nhấn [ENTER] để quay lại Menu chính...");
            scanner.nextLine();
        }
    }

    private static void executeChoice(String choice) {
        switch (choice) {
            case "1" -> CustomDriverDemo.run();
            case "2" -> DriverManagerDeepDiveDemo.run();
            case "3" -> {
                RawConnectionCostDemo.run();
                ResourceLeakDemo.run();
            }
            case "4" -> {
                SqlInjectionExploitDemo.run();
                PreparedStatementDeepDiveDemo.run();
            }
            case "5" -> {
                CursorAndFetchSizeDemo.run();
                ResultSetMetadataDemo.run();
            }
            case "6" -> {
                BankTransferTransactionDemo.run();
                SavepointDemo.run();
            }
            case "7" -> BatchInsertBenchmarkDemo.run();
            case "8" -> ConnectionPoolBenchmarkDemo.run();
            case "9" -> MiniOrmDemo.run();
            case "10" -> runAllLabs();
            default -> System.out.println("⚠️ Lựa chọn không hợp lệ, vui lòng chọn từ 0 đến 10.");
        }
    }

    private static void printMenu() {
        System.out.println("\n=======================================================");
        System.out.println("   KHÁM PHÁ BẢN CHẤT CỐT LÕI CỦA JDBC (JDBC UNDER THE HOOD)");
        System.out.println("=======================================================");
        System.out.println(" [1] Bản chất JDBC Interface & Tự viết Custom Driver");
        System.out.println(" [2] Cơ chế SPI (Service Provider Interface) & DriverManager");
        System.out.println(" [3] Vòng đời Connection & Quản lý tài nguyên (try-with-resources)");
        System.out.println(" [4] Lỗ hổng SQL Injection & Sức mạnh của PreparedStatement");
        System.out.println(" [5] Bản chất ResultSet, Con trỏ Cursor & ResultSetMetaData");
        System.out.println(" [6] Giao dịch ACID trong ngân hàng (Commit/Rollback/Savepoint)");
        System.out.println(" [7] Batch Processing: Tăng tốc độ ghi dữ liệu gấp hàng chục lần");
        System.out.println(" [8] Tự viết Connection Pool (Mini HikariCP bằng Dynamic Proxy)");
        System.out.println(" [9] Mini ORM (Reflection) & Triển khai DAO thực chiến");
        System.out.println(" [10] CHẠY TOÀN BỘ CÁC BÀI THỰC HÀNH (Full Tour)");
        System.out.println(" [0] Thoát");
        System.out.println("=======================================================");
    }

    private static void runAllLabs() {
        System.out.println(">>> ĐANG CHẠY TOÀN BỘ CÁC BÀI THỰC HÀNH TỔNG HỢP <<<\n");
        CustomDriverDemo.run();
        DriverManagerDeepDiveDemo.run();
        RawConnectionCostDemo.run();
        ResourceLeakDemo.run();
        SqlInjectionExploitDemo.run();
        PreparedStatementDeepDiveDemo.run();
        CursorAndFetchSizeDemo.run();
        ResultSetMetadataDemo.run();
        BankTransferTransactionDemo.run();
        SavepointDemo.run();
        BatchInsertBenchmarkDemo.run();
        ConnectionPoolBenchmarkDemo.run();
        MiniOrmDemo.run();
        System.out.println("\n>>> HOÀN TẤT TẤT CẢ CÁC BÀI THỰC HÀNH! <<<");
    }
}
