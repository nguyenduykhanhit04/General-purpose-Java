package lesson04_resultset_cursor;

import config.DatabaseConfig;

import java.sql.*;

/**
 * ==============================================================================
 * BÀI 4.2: BẢN CHẤT RESULTSETMETADATA & CÁCH CÁC ORM (HIBERNATE / SPRING DATA) RA ĐỜI
 * ==============================================================================
 *
 * CÂU HỎI LỚN:
 * Làm thế nào các thư viện ORM như Hibernate, MyBatis, hay Spring Data JDBC có thể tự động
 * đọc bất kỳ bảng dữ liệu nào của bạn và chuyển nó thành đối tượng Java tương ứng?
 *
 * CÂU TRẢ LỜI:
 * Nhờ vào `ResultSetMetaData` (Siêu dữ liệu của tập kết quả)!
 * - Nó cung cấp toàn bộ thông tin cấu trúc của các cột trả về:
 *   + Tên cột (Column Name / Label).
 *   + Kiểu dữ liệu SQL (SQL Type code: Types.VARCHAR, Types.INTEGER, Types.DOUBLE...).
 *   + Tên kiểu tương ứng (VARCHAR, INT...).
 *   + Kích thước tối đa, có cho phép NULL không.
 *
 * Bằng cách kết hợp `ResultSetMetaData` với `Java Reflection API`, ta có thể viết được
 * một Mini ORM tự động ánh xạ dữ liệu (chúng ta sẽ chứng minh ở Bài 8)!
 * ==============================================================================
 */
public class ResultSetMetadataDemo {

    public static void run() {
        System.out.println("\n=======================================================");
        System.out.println("LAB 4.2: BẢN CHẤT RESULTSETMETADATA (SOI CẤU TRÚC BẢNG ĐỘNG)");
        System.out.println("=======================================================");

        DatabaseConfig.initDatabaseSchema();

        String sql = "SELECT * FROM students LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Lấy đối tượng siêu dữ liệu
            ResultSetMetaData metaData = rs.getMetaData();

            int columnCount = metaData.getColumnCount();
            System.out.println("1. Phân tích cấu trúc bảng 'students' động lúc Runtime:");
            System.out.printf("   Tổng số cột: %d%n%n", columnCount);

            System.out.printf("   %-5s | %-15s | %-15s | %-12s | %-10s%n",
                    "STT", "Tên cột", "Kiểu SQL (Tên)", "Mã kiểu (int)", "Cho phép Null?");
            System.out.println("   -------------------------------------------------------------------");

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                String columnTypeName = metaData.getColumnTypeName(i);
                int columnType = metaData.getColumnType(i);
                int nullable = metaData.isNullable(i);
                String nullableStr = (nullable == ResultSetMetaData.columnNullable) ? "YES" : "NO";

                System.out.printf("   %-5d | %-15s | %-15s | %-12d | %-10s%n",
                        i, columnName, columnTypeName, columnType, nullableStr);
            }

            System.out.println("\nKẾT LUẬN:");
            System.out.println("   - Nhờ ResultSetMetaData, Java có thể 'soi thấu' cấu trúc bảng ngay cả khi câu SQL");
            System.out.println("     được viết tùy biến (vd: SELECT s.name, a.balance FROM ... JOIN ...).");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
