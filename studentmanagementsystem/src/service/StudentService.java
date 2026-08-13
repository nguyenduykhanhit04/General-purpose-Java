package service;

import model.Student;
import repository.StudentRepository;

import java.util.List;
import java.util.Optional;

public class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public void addStudent(Student student) {
        if (studentRepository.findById(student.getId()).isPresent()) {
            throw new RuntimeException("Student ID already exists");
        }

        studentRepository.add(student);
    }

    public void upateStudent(Student student) {
        if (studentRepository.findById(student.getId()).isEmpty()) {
            throw new RuntimeException("Student not found");
        }

        studentRepository.update(student);
    }

    public void deleteStudent(String id) {
        if (studentRepository.findById(id).isEmpty()) {
            throw new RuntimeException("Student not found");
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
