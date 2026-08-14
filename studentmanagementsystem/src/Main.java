import exception.DuplicateStudentException;
import exception.InvalidStudentException;
import exception.StudentNotFoundException;
import model.Gender;
import model.Student;
import repository.StudentRepository;
import service.StudentService;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {

        StudentRepository studentRepository = new StudentRepository();

        StudentService studentService = new StudentService(studentRepository);

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

        // CREATE
        try {
            studentService.addStudent(student1);
            studentService.addStudent(student2);
        } catch (DuplicateStudentException e) {
            System.out.println(e.getMessage());
        }

        // READ ALL
        System.out.println("=== BEFORE UPDATE ===");
        for (Student student : studentService.getAllStudents()) {
            System.out.println(student);
        }

        // FIND BY ID
        System.out.println("\n=== FIND SV002 ===");
        studentService.getStudentById("SV002")
                .ifPresent(System.out::println);

        // UPDATE
        Student updatedStudent = new Student(
                "SV002",
                "Nguyen Van B",
                21,
                "b@gmail.com",
                Gender.FEMALE,
                7.5,
                student2.getCreatedAt()
        );

        student2.setAge(55);
        studentService.upateStudent(student2);
//        repository.update(updatedStudent);

        // DELETE
        try {
            studentService.deleteStudent("SV001");
        } catch (StudentNotFoundException e) {
            System.out.println(e.getMessage());
        }

        // READ ALL
        System.out.println("\n=== AFTER UPDATE + DELETE ===");
        for (Student student : studentService.getAllStudents()) {
            System.out.println(student);
        }

        // Test Invalid Student Exception
        Student invalidStudent = new Student(
                "SV003",
                "",
                -5,
                "abc",
                Gender.MALE,
                15,
                LocalDateTime.now()
        );

        try {
            studentService.addStudent(invalidStudent);
        } catch (InvalidStudentException e) {
            System.out.println(e.getMessage());
        }
    }
}