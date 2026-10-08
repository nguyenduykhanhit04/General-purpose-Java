package lesson08_mini_orm_and_dao;

import config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * ==============================================================================
 * BÀI 8.3: THỬ NGHIỆM CRUD DAO & ÁNH XẠ MINI ORM
 * ==============================================================================
 */
public class MiniOrmDemo {

    public static void run() {
        System.out.println("\n=======================================================");
        System.out.println("LAB 8: TRIỂN KHAI DAO HOÀN CHỈNH & MINI REFLECTION ORM");
        System.out.println("=======================================================");

        DatabaseConfig.initDatabaseSchema();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM students");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        StudentDao studentDao = new StudentDaoJdbcImpl();

        // 1. Thêm mới sinh viên qua DAO
        System.out.println("1. Thêm mới 3 sinh viên qua StudentDaoJdbcImpl:");
        studentDao.save(new Student("SV001", "Nguyen Van An", 21, "an.nguyen@email.com", "MALE", 8.8));
        studentDao.save(new Student("SV002", "Tran Thi Binh", 20, "binh.tran@email.com", "FEMALE", 9.2));
        studentDao.save(new Student("SV003", "Le Hoang Cuong", 22, "cuong.le@email.com", "MALE", 7.5));
        System.out.println("   -> Đã lưu 3 sinh viên thành công!");

        // 2. Tìm tất cả
        System.out.println("\n2. Đọc toàn bộ sinh viên:");
        List<Student> allStudents = studentDao.findAll();
        allStudents.forEach(s -> System.out.println("   -> " + s));

        // 3. Tìm theo ID
        System.out.println("\n3. Tìm sinh viên ID 'SV002':");
        studentDao.findById("SV002").ifPresentOrElse(
                s -> System.out.println("   -> Tìm thấy: " + s),
                () -> System.out.println("   -> Không tìm thấy!")
        );

        // 4. Cập nhật
        System.out.println("\n4. Cập nhật GPA sinh viên 'SV001' từ 8.8 thành 9.5:");
        studentDao.findById("SV001").ifPresent(s -> {
            s.setGpa(9.5);
            s.setName("Nguyen Van An (Đã cập nhật)");
            studentDao.update(s);
        });
        studentDao.findById("SV001").ifPresent(s -> System.out.println("   -> Sau cập nhật: " + s));

        // 5. Tìm kiếm theo tên (LIKE %keyword%)
        System.out.println("\n5. Tìm kiếm sinh viên có tên chứa chữ 'Thi':");
        studentDao.searchByName("Thi").forEach(s -> System.out.println("   -> Kết quả: " + s));

        // 6. Trình diễn Mini Generic ORM bằng Reflection
        System.out.println("\n6. Trình diễn MINI GENERIC ORM (Tự động ánh xạ ResultSet sang Student.class bằng Reflection):");
        demonstrateMiniGenericOrm();
    }

    private static void demonstrateMiniGenericOrm() {
        String sql = "SELECT id, name, age, email, gender, gpa, created_at FROM students WHERE id = 'SV002'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                // Tự động map không cần dòng code gán tay nào!
                Student student = MiniRowMapper.autoMap(rs, Student.class);
                System.out.println("   [MINI ORM THÀNH CÔNG] Đối tượng được tạo tự động qua Reflection:");
                System.out.println("   -> ID: " + student.getId());
                System.out.println("   -> Name: " + student.getName());
                System.out.println("   -> GPA: " + student.getGpa());
                System.out.println("   -> CreatedAt: " + student.getCreatedAt());
                System.out.println("   => BẢN CHẤT: Hibernate và Spring JdbcTemplate cũng vận hành dựa trên cơ chế này!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
