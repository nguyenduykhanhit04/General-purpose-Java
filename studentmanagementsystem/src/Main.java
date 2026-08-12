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

        repository.add(student1);

        for (Student student : repository.findAll()) {
            System.out.println(student);
        }
    }
}