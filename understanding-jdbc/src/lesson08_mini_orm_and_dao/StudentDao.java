package lesson08_mini_orm_and_dao;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO chuẩn mực trong thiết kế tầng dữ liệu (Data Access Object).
 * Tách biệt hoàn toàn logic nghiệp vụ (Service) khỏi chi tiết truy cập database (JDBC, Hibernate...).
 */
public interface StudentDao {
    void save(Student student);
    Optional<Student> findById(String id);
    List<Student> findAll();
    void update(Student student);
    void deleteById(String id);
    List<Student> searchByName(String nameKeyword);
}
