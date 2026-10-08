package lesson08_mini_orm_and_dao;

import java.time.LocalDateTime;

public class Student {
    private String id;
    private String name;
    private int age;
    private String email;
    private String gender;
    private double gpa;
    private LocalDateTime createdAt;

    // No-arg constructor cần thiết cho Reflection / ORM
    public Student() {
    }

    public Student(String id, String name, int age, String email, String gender, double gpa) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
        this.gender = gender;
        this.gpa = gpa;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("Student[id='%s', name='%s', age=%d, email='%s', gender='%s', gpa=%.2f]",
                id, name, age, email, gender, gpa);
    }
}
