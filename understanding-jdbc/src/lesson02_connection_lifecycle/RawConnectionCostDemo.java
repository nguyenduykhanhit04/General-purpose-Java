package lesson02_connection_lifecycle;

import config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ==============================================================================
 * BÀI 2.1: BẢN CHẤT CỦA CONNECTION & CHI PHÍ KHỞI TẠO (CONNECTION OVERHEAD)
 * ==============================================================================
 *
 * BẢN CHẤT CỦA CONNECTION LÀ GÌ?
 * 1. Ở tầng Mạng (Network): Connection thực chất là một Socket TCP được mở giữa máy Client và Database Server.
 *    Để mở 1 Connection, phải trải qua:
 *    - Phân giải DNS / Tìm địa chỉ IP.
 *    - Bắt tay 3 bước TCP (TCP 3-way Handshake: SYN -> SYN-ACK -> ACK).
 *    - Bắt tay mã hóa TLS/SSL (nếu bật).
 *    - Bắt tay xác thực tài khoản (Authentication: gửi username, password hash).
 *    - Phía Database Server: Tạo một tiến trình con (Process) hoặc Luồng (Thread) riêng biệt,
 *      cấp phát bộ nhớ RAM riêng (Session buffer, Sort buffer, Temporary tablespace) cho kết nối đó!
 *
 * 2. HẬU QUẢ NẾU MỖI CÂU LỆNH SQL LẠI MỞ 1 KẾT NỐI MỚI:
 *    - Thời gian mở kết nối có thể chậm gấp 10 - 100 lần thời gian thực thi câu lệnh SQL!
 *    - Database Server cạn kiệt tài nguyên (Max connections reached, Out of Memory).
 *    - Đó là lý do CẦN PHẢI CÓ CONNECTION POOL (chúng ta sẽ tự code ở Bài 7)!
 * ==============================================================================
 */
public class RawConnectionCostDemo {

    public static void run() {
        System.out.println("\n=======================================================");
        System.out.println("LAB 2.1: CHI PHÍ TẠO CONNECTION & BẢN CHẤT TÁI SỬ DỤNG");
        System.out.println("=======================================================");

        int iterations = 100;

        // Cách 1: Mỗi thao tác lại mở 1 Connection mới rồi đóng
        System.out.printf("1. Đang chạy %d truy vấn bằng cách MỞ & ĐÓNG CONNECTION MỚI liên tục...%n", iterations);
        long start1 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT 1");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        long duration1 = System.currentTimeMillis() - start1;
        System.out.printf("   -> Thời gian (Mở mới 100 Connection liên tục): %d ms%n", duration1);

        // Cách 2: Tái sử dụng 1 Connection duy nhất
        System.out.printf("\n2. Đang chạy %d truy vấn bằng cách TÁI SỬ DỤNG 1 CONNECTION DUY NHẤT...%n", iterations);
        long start2 = System.currentTimeMillis();
        try (Connection conn = DatabaseConfig.getConnection()) {
            for (int i = 0; i < iterations; i++) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT 1");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        long duration2 = System.currentTimeMillis() - start2;
        System.out.printf("   -> Thời gian (Tái sử dụng 1 Connection): %d ms%n", duration2);

        System.out.println("\nKẾT LUẬN BẢN CHẤT:");
        System.out.printf("   - Tái sử dụng nhanh hơn khoảng %.1f lần ngay cả trên In-Memory DB!%n",
                (double) duration1 / (duration2 == 0 ? 1 : duration2));
        System.out.println("   - Đối với MySQL / Postgres qua mạng Internet/LAN, sự chênh lệch có thể lên đến hàng trăm lần do độ trễ TCP!");
    }
}
