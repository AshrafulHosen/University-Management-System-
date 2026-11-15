package com.example.unimanage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String studentNumber;

    @Column(nullable = false)
    private String department;

    public Student() {
    }

    public Student(
            String name,
            String email,
            String studentNumber,
            String department
    ) {
        this.name = name;
        this.email = email;
        this.studentNumber = studentNumber;
        this.department = department;
    }

    public Student(
            String username,
            String name,
            String email,
            String studentNumber,
            String department
    ) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.studentNumber = studentNumber;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
