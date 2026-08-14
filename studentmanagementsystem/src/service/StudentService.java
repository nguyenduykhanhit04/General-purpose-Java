package service;

import exception.DuplicateStudentException;
import exception.InvalidStudentException;
import exception.StudentNotFoundException;
import model.Student;
import repository.StudentRepository;

import java.util.List;
import java.util.Optional;

public class StudentService {
    private final StudentRepository studentRepository;

    private void validateStudent(Student student) {
        if (student.getId() == null || student.getId().isBlank()) {
            throw new InvalidStudentException("Student ID cannot be empty");
        }

        if (student.getName() == null || student.getName().isBlank()) {
            throw new InvalidStudentException("Student name cannot be empty");
        }

        if (student.getAge() <= 0) {
            throw new InvalidStudentException("Student age must be greater than 0");
        }

        if (student.getGpa() < 0 || student.getGpa() > 10) {
            throw new InvalidStudentException("Student GPA must be between 0 and 10");
        }

        if (student.getGender() == null) {
            throw new InvalidStudentException("Student gender cannot be null");
        }

        if (student.getEmail() == null ||
            !student.getEmail().contains("@")) {
            throw new InvalidStudentException("Student email is invalid");
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

    public void upateStudent(Student student) {
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
