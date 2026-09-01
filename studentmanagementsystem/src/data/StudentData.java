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

        Student student3 = new Student(
                "SV003",
                "Nguyen Van C",
                22,
                "C@gmail.com",
                Gender.MALE,
                9.8,
                LocalDateTime.now()
        );

        Student student4 = new Student(
                "SV004",
                "Le Van A",
                24,
                "la@gmail.com",
                Gender.FEMALE,
                8.6,
                LocalDateTime.now()
        );

        Student student5 = new Student(
                "SV005",
                "Le Van B",
                20,
                "lb@gmail.com",
                Gender.MALE,
                8.8,
                LocalDateTime.now()
        );

        Student student6 = new Student(
                "SV006",
                "Le Van C",
                29,
                "lc@gmail.com",
                Gender.MALE,
                8.5,
                LocalDateTime.now()
        );

        return List.of(student1, student2, student3, student4, student5, student6);
    }
}
