package service;

import exception.DuplicateStudentException;
import exception.StudentNotFoundException;
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
            throw new DuplicateStudentException("Student ID already exists" + student.getId());
        }

        studentRepository.add(student);
    }

    public void upateStudent(Student student) {
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
