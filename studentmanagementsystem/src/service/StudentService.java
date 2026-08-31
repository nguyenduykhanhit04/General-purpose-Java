package service;

import exception.DuplicateStudentException;
import exception.InvalidStudentException;
import exception.StudentNotFoundException;
import model.Student;
import repository.StudentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentService {
    private final StudentRepository studentRepository;

    private void validateStudent(Student student) {

        List<String> errors = new ArrayList<>();

        if (student.getId() == null || student.getId().isBlank()) {
            errors.add("Student ID cannot be empty");
        }

        if (student.getName() == null || student.getName().isBlank()) {
           errors.add("Student name cannot be empty");
        }

        if (student.getAge() <= 0) {
            errors.add("Student age must be greater than 0");
        }

        if (student.getGpa() < 0 || student.getGpa() > 10) {
            errors.add("Student GPA must be between 0 and 10");
        }

        if (student.getGender() == null) {
            errors.add("Student gender cannot be null");
        }

        if (student.getEmail() == null ||
            !student.getEmail().contains("@")) {
            errors.add("Student email is invalid");
        }

        if (!errors.isEmpty()) {
            throw new InvalidStudentException(
                    String.join(", ", errors)
            );
        }
    }

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public void addStudent(Student student) {
        validateStudent(student);

        if (studentRepository.findById(student.getId()).isPresent()) {
            throw new DuplicateStudentException("Student ID already exists" + student.getId());
        }

        studentRepository.add(student);
    }

    public void updateStudent(Student student) {
        validateStudent(student);

        if (studentRepository.findById(student.getId()).isEmpty()) {
            throw new StudentNotFoundException("Student not found" + student.getId());
        }

        studentRepository.update(student);
    }

    public void deleteStudent(String id) {
        if (studentRepository.findById(id).isEmpty()) {
            throw new StudentNotFoundException("Student not found" + id);
        }

        studentRepository.delete(id);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(String id) {
        return studentRepository.findById(id);
    }
}
