package repository;

import model.Student;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentRepository {
    // Đây chính là nơi tạm thời lưu dữ liệu Student.
    // final ở đây không có nghĩa là danh sách không thể thêm phần tử
    // Nó có nghĩa là biến students không được trỏ sang một List khác sau khi khởi tạo.
    private final List<Student> students = new ArrayList<>();

    public void add(Student student) {
        students.add(student);
    }

    public List<Student> findAll() {
        // List.copyOf(students) tạo ra một List không thể chỉnh sửa từ danh sách hiện tại.
        // sẽ không xóa được danh sách bên trong Repository
        // Đây là một cách bảo vệ dữ liệu bên trong object → liên quan trực tiếp đến encapsulation.
        return List.copyOf(students);
    }

    public Optional<Student> findById(String id) {
        return students.stream() // → lấy danh sách Student để xử lý.
                // → chỉ giữ lại Student có ID bằng ID cần tìm.
                .filter(student -> student.getId().equals(id))
                // → lấy Student đầu tiên tìm thấy.
                .findFirst();
    }

//    public void delete(String id) {
//        Optional<Student> result = findById(id);
//
//        result.ifPresent(student -> {
//            students.remove(student);
//        });
//    }

    public void delete(String id) {
        students.removeIf(student -> student.getId().equals(id));
    }

    public void update(Student student) {
        Optional<Student> existingStudent  =  findById(student.getId());

        existingStudent.ifPresent(oldStudent -> {
            int index = students.indexOf(oldStudent); // Tìm vị trí của Student cũ.
            students.set(index, student); // Thay phần tử cũ bằng Student mới.
        });

    }
}
