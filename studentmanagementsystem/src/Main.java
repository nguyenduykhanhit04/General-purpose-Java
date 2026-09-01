import data.StudentData;
import exception.DuplicateStudentException;
import exception.InvalidStudentException;
import exception.StudentNotFoundException;
import model.Gender;
import model.Student;
import repository.StudentRepository;
import service.StudentService;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        StudentRepository studentRepository =
                new StudentRepository();

        StudentService studentService =
                new StudentService(studentRepository);

        // =========================
        // CREATE
        // =========================

        List<Student> students =
                StudentData.createStudents();

        try {
            for (Student student : students) {
                studentService.addStudent(student);
            }
        } catch (DuplicateStudentException e) {
            System.out.println(e.getMessage());
        }

        // =========================
        // READ ALL
        // =========================

        System.out.println("=== BEFORE UPDATE ===");

        for (Student student : studentService.getAllStudents()) {
            System.out.println(student);
        }

        // =========================
        // FIND BY ID
        // =========================

        System.out.println("\n=== FIND SV002 ===");

        studentService.getStudentById("SV002")
                .ifPresent(System.out::println);

        // =========================
        // UPDATE
        // =========================

        Student student2 = studentService
                .getStudentById("SV002")
                .orElseThrow();

        student2.setAge(55);

        studentService.updateStudent(student2);

        // =========================
        // DELETE
        // =========================

        try {
            studentService.deleteStudent("SV001");
        } catch (StudentNotFoundException e) {
            System.out.println(e.getMessage());
        }

        // =========================
        // READ ALL
        // =========================

        System.out.println("\n=== AFTER UPDATE + DELETE ===");

        for (Student student : studentService.getAllStudents()) {
            System.out.println(student);
        }

        // =========================
        // INVALID STUDENT
        // =========================

        Student invalidStudent = new Student(
                "SV003",
                "",
                -5,
                "abc",
                Gender.MALE,
                15,
                java.time.LocalDateTime.now()
        );

        try {
            studentService.addStudent(invalidStudent);
        } catch (InvalidStudentException e) {
            System.out.println(e.getMessage());
        }

        // =========================
        // SEARCH
        // =========================

        List<Student> resultName =
                studentService.searchByName("Nguyen");

        List<Student> resultGPA =
                studentService.searchByGPA(10.0);

        List<Student> resultAge =
                studentService.searchByAge(55);

        List<Student> resultGender =
                studentService.searchByGender(Gender.FEMALE);

        System.out.println("\n=== SEARCH RESULT ===");

        for (Student student : resultName) {
            System.out.println(student);
        }

        for (Student student : resultGPA) {
            System.out.println(student);
        }

        for (Student student : resultAge) {
            System.out.println(student);
        }

        for (Student student : resultGender) {
            System.out.println(student);
        }

        // =========================
        // SORT
        // =========================
        System.out.println("\n=== SORT BY GPA ===");
        List<Student> resultSortGPA = studentService.sortByGPA();
        List<Student> resultSortGPAReversed = studentService.sortByGPAReversed();
        List<Student> resultSortName = studentService.sortByName();
        List<Student> resultSortNameAndGPA = studentService.sortByNameThenGpa();
        List<Student> resultSortNameAndGPADesc = studentService.sortByNameThenByGPADesc();
        for (Student student : resultSortGPA) {
            System.out.println(student);
        }
        for (Student student : resultSortGPAReversed) {
            System.out.println(student);
        }
        for (Student student : resultSortName) {
            System.out.println(student);
        }
        for (Student student : resultSortNameAndGPA) {
            System.out.println(student);
        }
        for (Student student : resultSortNameAndGPADesc) {
            System.out.println(student);
        }
    }
}