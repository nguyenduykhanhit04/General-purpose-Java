package lesson08_mini_orm_and_dao;

import config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ==============================================================================
 * BÀI 8.2: TRIỂN KHAI THỰC CHIẾN STUDENT DAO BẰNG JDBC CHUẨN DOANH NGHIỆP
 * ==============================================================================
 *
 * CÁC NGUYÊN TẮC BẮT BUỘC KHI VIẾT JDBC PRODUCTION:
 * 1. Luôn dùng PreparedStatement, KHÔNG BAO GIỜ cộng chuỗi SQL.
 * 2. Luôn dùng try-with-resources để tự động giải phóng Connection, Statement, ResultSet.
 * 3. Chuyển đổi SQLException thành Unchecked RuntimeException hoặc DataAccessException
 *    để không làm ô nhiễm chữ ký phương thức tầng Service.
 * 4. Tách biệt ánh xạ ResultSet ra mapper độc lập để tái sử dụng.
 * ==============================================================================
 */
public class StudentDaoJdbcImpl implements StudentDao {

    @Override
    public void save(Student student) {
        String sql = "INSERT INTO students (id, name, age, email, gender, gpa, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getId());
            ps.setString(2, student.getName());
            ps.setInt(3, student.getAge());
            ps.setString(4, student.getEmail());
            ps.setString(5, student.getGender());
            ps.setDouble(6, student.getGpa());
            ps.setTimestamp(7, Timestamp.valueOf(student.getCreatedAt()));

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu sinh viên id=" + student.getId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Student> findById(String id) {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Sử dụng RowMapper tái sử dụng
                    return Optional.of(MiniRowMapper.STUDENT_MANUAL_MAPPER.mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm sinh viên id=" + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Student> findAll() {
        String sql = "SELECT * FROM students ORDER BY id";
        List<Student> list = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(MiniRowMapper.STUDENT_MANUAL_MAPPER.mapRow(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách sinh viên: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Student student) {
        String sql = "UPDATE students SET name = ?, age = ?, email = ?, gender = ?, gpa = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getName());
            ps.setInt(2, student.getAge());
            ps.setString(3, student.getEmail());
            ps.setString(4, student.getGender());
            ps.setDouble(5, student.getGpa());
            ps.setString(6, student.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                System.out.println("   [CẢNH BÁO] Không tìm thấy sinh viên id=" + student.getId() + " để cập nhật.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi cập nhật sinh viên id=" + student.getId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa sinh viên id=" + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Student> searchByName(String nameKeyword) {
        String sql = "SELECT * FROM students WHERE LOWER(name) LIKE ? ORDER BY name";
        List<Student> list = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Dùng dấu % trong tham số thay vì cộng chuỗi vào SQL
            ps.setString(1, "%" + nameKeyword.toLowerCase() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(MiniRowMapper.STUDENT_MANUAL_MAPPER.mapRow(rs));
                }
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tìm kiếm sinh viên theo tên: " + e.getMessage(), e);
        }
    }
}
