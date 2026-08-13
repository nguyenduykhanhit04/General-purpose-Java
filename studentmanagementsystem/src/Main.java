import model.Gender;
import model.Student;
import repository.StudentRepository;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {

        StudentRepository repository = new StudentRepository();

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
        repository.add(student1);
        repository.add(student2);

        // READ ALL
        System.out.println("=== BEFORE UPDATE ===");
        for (Student student : repository.findAll()) {
            System.out.println(student);
        }

        // FIND BY ID
        System.out.println("\n=== FIND SV002 ===");
        repository.findById("SV002")
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
        repository.update(student2);
//        repository.update(updatedStudent);

        // DELETE
        repository.delete("SV001");

        // READ ALL
        System.out.println("\n=== AFTER UPDATE + DELETE ===");
        for (Student student : repository.findAll()) {
            System.out.println(student);
        }
    }
}