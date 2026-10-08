package lesson08_mini_orm_and_dao;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * ==============================================================================
 * BÀI 8.1: BẢN CHẤT CỦA ROWMAPPER & CÁCH SPRING JDBCTEMPLATE / ORM ÁNH XẠ DỮ LIỆU
 * ==============================================================================
 *
 * 1. ROWMAPPER LÀ GÌ?
 *    - Là một Functional Interface nhận vào ResultSet và biến dòng dữ liệu hiện tại thành một Object Java <T>.
 *    - Trong Spring Framework, `org.springframework.jdbc.core.RowMapper` hoạt động y hệt như interface dưới đây!
 *
 * 2. TỰ ĐỘNG ÁNH XẠ BẰNG REFLECTION (MINI ORM):
 *    - Thay vì viết tay `student.setName(rs.getString("name"))` lặp đi lặp lại cho 50 thuộc tính,
 *      ORM dùng `ResultSetMetaData` để lấy danh sách tên cột, sau đó dùng `Reflection API`
 *      tìm trường (Field) tương ứng trong Class Java và gán giá trị vào!
 * ==============================================================================
 */
public class MiniRowMapper {

    @FunctionalInterface
    public interface RowMapper<T> {
        T mapRow(ResultSet rs) throws SQLException;
    }

    /**
     * Mapper viết tay thủ công (High performance, rõ ràng, kiểu an toàn)
     */
    public static final RowMapper<Student> STUDENT_MANUAL_MAPPER = rs -> {
        Student s = new Student();
        s.setId(rs.getString("id"));
        s.setName(rs.getString("name"));
        s.setAge(rs.getInt("age"));
        s.setEmail(rs.getString("email"));
        s.setGender(rs.getString("gender"));
        s.setGpa(rs.getDouble("gpa"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            s.setCreatedAt(ts.toLocalDateTime());
        }
        return s;
    };

    /**
     * MINI GENERIC ORM: Tự động ánh xạ bất kỳ bảng nào sang bất kỳ Java Class nào!
     * (Đây chính là bí thuật bên trong Spring BeanPropertyRowMapper và Hibernate)
     */
    public static <T> T autoMap(ResultSet rs, Class<T> clazz) {
        try {
            // 1. Tạo instance mới của class bằng default constructor
            T instance = clazz.getDeclaredConstructor().newInstance();

            // 2. Lấy metadata các cột từ ResultSet
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 3. Duyệt từng cột và gán giá trị vào field trùng tên
            for (int i = 1; i <= columnCount; i++) {
                String rawColName = metaData.getColumnLabel(i); // ví dụ: "created_at" hoặc "name"
                String normalizedFieldName = toCamelCase(rawColName);

                try {
                    Field field = findField(clazz, rawColName, normalizedFieldName);
                    if (field != null) {
                        field.setAccessible(true);
                        Object value = rs.getObject(i);

                        // Xử lý chuyển đổi kiểu đặc biệt (ví dụ SQL Timestamp -> LocalDateTime)
                        if (value instanceof Timestamp && field.getType().equals(LocalDateTime.class)) {
                            value = ((Timestamp) value).toLocalDateTime();
                        }

                        field.set(instance, value);
                    }
                } catch (NoSuchFieldException ignored) {
                    // Không có field tương ứng trong class thì bỏ qua
                }
            }

            return instance;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi ánh xạ tự động ORM sang " + clazz.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    private static Field findField(Class<?> clazz, String colName, String camelCaseName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(camelCaseName);
        } catch (NoSuchFieldException e1) {
            try {
                return clazz.getDeclaredField(colName.toLowerCase());
            } catch (NoSuchFieldException e2) {
                return null;
            }
        }
    }

    // Chuyển đổi snake_case (created_at) thành camelCase (createdAt)
    private static String toCamelCase(String s) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (char c : s.toLowerCase().toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }
}
