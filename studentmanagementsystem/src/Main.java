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

        Student updatedStudent = new Student(
                "SV002",
                "Nguyen Van B",
                21,
                "b@gmail.com",
                Gender.FEMALE,
                7.5,
                student2.getCreatedAt()
        );

        repository.add(student1);
        repository.add(student2);
        repository.findAll();
        repository.findById("SV002");
        repository.delete("SV001");
        repository.update(updatedStudent);

        for (Student student : repository.findAll()) {
            System.out.println(student);
        }
    }
}