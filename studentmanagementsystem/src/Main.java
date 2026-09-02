import data.StudentData;
import exception.DuplicateStudentException;
import exception.InvalidStudentException;
import exception.StudentNotFoundException;
import model.Gender;
import model.Student;
import repository.StudentRepository;
import service.StudentService;

import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        StudentService studentService =
                new StudentService(new StudentRepository());

        // CREATE
        try {
            StudentData.createStudents()
                    .forEach(studentService::addStudent);
        } catch (DuplicateStudentException e) {
            System.out.println(e.getMessage());
        }

        // READ ALL
        printStudents("=== BEFORE UPDATE ===", studentService.getAllStudents());

        // FIND BY ID
        System.out.println("\n=== FIND SV002 ===");
        studentService.getStudentById("SV002")
                .ifPresent(System.out::println);

        // UPDATE
        Student student = studentService.getStudentById("SV002")
                .orElseThrow();

        student.setAge(55);
        studentService.updateStudent(student);

        // DELETE
        try {
            studentService.deleteStudent("SV001");
        } catch (StudentNotFoundException e) {
            System.out.println(e.getMessage());
        }

        // READ ALL
        printStudents(
                "\n=== AFTER UPDATE + DELETE ===",
                studentService.getAllStudents()
        );

        // INVALID STUDENT
        Student invalidStudent = new Student(
                "SV003", "", -5, "abc",
                Gender.MALE, 15, LocalDateTime.now()
        );

        try {
            studentService.addStudent(invalidStudent);
        } catch (InvalidStudentException e) {
            System.out.println(e.getMessage());
        }

        // SEARCH
        System.out.println("\n=== SEARCH RESULT ===");

        printStudents(studentService.searchByName("Nguyen"));
        printStudents(studentService.searchByGPA(10.0));
        printStudents(studentService.searchByAge(55));
        printStudents(studentService.searchByGender(Gender.FEMALE));

        // SORT
        System.out.println("\n=== SORT RESULT ===");

        printStudents(studentService.sortByGPA());
        printStudents(studentService.sortByGPAReversed());
        printStudents(studentService.sortByName());
        printStudents(studentService.sortByNameThenGpa());
        printStudents(studentService.sortByNameThenByGPADesc());

        // SEARCH WITH CONDITION
        System.out.println("\n=== FEMALE WITH GPA > 8.0 ===");

        printStudents(
                studentService.searchFemaleStudentsWithHighGPA(8.0)
        );
    }

    private static void printStudents(List<Student> students) {
        students.forEach(System.out::println);
    }

    private static void printStudents(String title, List<Student> students) {
        System.out.println(title);
        printStudents(students);
    }
}
