package lesson06_batch_processing;

import config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * ==============================================================================
 * BÀI 6: BẢN CHẤT BATCH PROCESSING (XỬ LÝ LÔ) & BENCHMARK HIỆU NĂNG
 * ==============================================================================
 *
 * TẠI SAO CHÈN NHIỀU DỮ LIỆU BẰNG VÒNG LẶP LẠI RẤT CHẬM?
 * - Giả sử cần chèn 1,000 bản ghi.
 * - Cách đơn giản (Vòng lặp executeUpdate):
 *   + Java gửi 1 gói tin TCP sang Database.
 *   + Database ghi log, thực thi, gửi 1 gói tin ACK xác nhận về Java.
 *   + Lặp lại 1,000 lần => 1,000 lần di chuyển gói tin qua mạng (1,000 Network Roundtrips)!
 *   + Rất nhiều độ trễ mạng (Network Latency) bị lãng phí!
 *
 * BẢN CHẤT CỦA BATCH PROCESSING (addBatch / executeBatch):
 * - Java gom 200 hoặc 500 câu lệnh vào một gói tin duy nhất trong bộ nhớ đệm (Buffer).
 * - Khi gọi `executeBatch()`:
 *   + Gửi toàn bộ lô câu lệnh trong MỘT LẦN duy nhất qua Socket TCP.
 *   + Database thực thi hàng loạt và trả về mảng kết quả `int[]` chỉ trong 1 gói tin phản hồi!
 *   + Giảm số lượng Network Roundtrips từ 1,000 xuống còn 5 lần!
 * - Kết hợp với `conn.setAutoCommit(false)` để DB không phải flush ổ cứng (Disk I/O) cho từng dòng!
 * ==============================================================================
 */
public class BatchInsertBenchmarkDemo {

    private static final int RECORD_COUNT = 1000;

    public static void run() {
        System.out.println("\n=======================================================");
        System.out.println("LAB 6: BẢN CHẤT BATCH PROCESSING (SO SÁNH HIỆU NĂNG " + RECORD_COUNT + " BẢN GHI)");
        System.out.println("=======================================================");

        DatabaseConfig.initDatabaseSchema();

        // 1. Chèn đơn lẻ bằng vòng lặp thông thường
        long singleTime = benchmarkSingleInserts();

        // 2. Chèn hàng loạt bằng Batch Processing
        long batchTime = benchmarkBatchInserts();

        System.out.println("\n================== KẾT QUẢ BENCHMARK ==================");
        System.out.printf("1. Chèn đơn lẻ (Single Inserts)  : %,d ms%n", singleTime);
        System.out.printf("2. Chèn theo lô (Batch Processing): %,d ms%n", batchTime);
        if (batchTime > 0) {
            System.out.printf("=> Batch Processing NHANH HƠN GẤP: %.1f LẦN!%n", (double) singleTime / batchTime);
        }
        System.out.println("=======================================================");

        // Dọn dẹp dữ liệu benchmark để không làm ảnh hưởng các lab khác
        clearStudents();
    }

    private static long benchmarkSingleInserts() {
        clearStudents();
        String sql = "INSERT INTO students (id, name, age, email, gender, gpa) VALUES (?, ?, ?, ?, ?, ?)";

        long start = System.currentTimeMillis();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 1; i <= RECORD_COUNT; i++) {
                ps.setString(1, "SINGLE_" + i);
                ps.setString(2, "Student " + i);
                ps.setInt(3, 20);
                ps.setString(4, "student" + i + "@email.com");
                ps.setString(5, "MALE");
                ps.setDouble(6, 8.5);

                // Gửi từng câu một sang DB
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis() - start;
    }

    private static long benchmarkBatchInserts() {
        clearStudents();
        String sql = "INSERT INTO students (id, name, age, email, gender, gpa) VALUES (?, ?, ?, ?, ?, ?)";
        int batchSize = 200;

        long start = System.currentTimeMillis();
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            // TẮT AutoCommit để DB gom giao dịch, tránh ghi đĩa liên tục
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 1; i <= RECORD_COUNT; i++) {
                    ps.setString(1, "BATCH_" + i);
                    ps.setString(2, "Student " + i);
                    ps.setInt(3, 20);
                    ps.setString(4, "student" + i + "@email.com");
                    ps.setString(5, "MALE");
                    ps.setDouble(6, 8.5);

                    // Thêm câu lệnh vào bộ đệm Batch phía Client
                    ps.addBatch();

                    // Cứ đủ 200 câu thì gửi 1 đợt qua mạng
                    if (i % batchSize == 0 || i == RECORD_COUNT) {
                        ps.executeBatch();
                    }
                }
            }

            // Chốt giao dịch một lần duy nhất
            conn.commit();

        } catch (SQLException e) {
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
        return System.currentTimeMillis() - start;
    }

    private static void clearStudents() {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM students")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
