package data;

import model.Gender;
import model.Student;

import java.time.LocalDateTime;
import java.util.List;

public class StudentData {
    public static List<Student> createStudents() {
        Student student1 = new Student(
                "SV001",
                "Nguyen Van A",
                20,
                "a@gmail.com",
                Gender.MALE,
                8.5,
                LocalDateTime.now()
        );

        Student student2 = new Student(
                "SV002",
                "Nguyen Van B",
                21,
                "b@gmail.com",
                Gender.FEMALE,
                9.5,
                LocalDateTime.now()
        );

        return List.of(student1, student2);
    }
}
