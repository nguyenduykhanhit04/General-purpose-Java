package model;

import java.time.LocalDateTime;

public class Student {
    private String id;
    private String name;
    private int age;
    private String email;
    private Gender gender;
    private double gpa;
    private LocalDateTime createdAt;

    public Student(String id,
                   String name,
                   int age,
                   String email,
                   Gender gender,
                   double gpa,
                   LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
        this.gender = gender;
        this.gpa = gpa;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    // Có thể đọc ID, nhưng không thể thay đổi ID sau khi Student được tạo.

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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
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
        return "Student{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", age='" + age + '\'' +
                ", email='" + email + '\'' +
                ", gender=" + gender +
                ", gpa=" + gpa +
                ", createdAt=" + createdAt +
                '}';
    }
}
