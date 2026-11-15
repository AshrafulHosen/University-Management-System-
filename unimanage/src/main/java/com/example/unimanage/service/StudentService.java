package com.example.unimanage.service;

import com.example.unimanage.entity.Student;
import com.example.unimanage.repository.StudentRepository;
import com.example.unimanage.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public StudentService(StudentRepository studentRepository, UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student not found"));
    }

    @Transactional(readOnly = true)
    public Student getStudentByUsername(String username) {
        return studentRepository.findByUsername(username)
                .or(() -> studentRepository.findByEmail(username))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student record not found for username: " + username));
    }

    public Student createStudent(Student student) {
        if (student.getName() == null || student.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required.");
        }
        if (student.getEmail() == null || student.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required.");
        }
        if (student.getStudentNumber() == null || student.getStudentNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student number is required.");
        }
        if (studentRepository.findAll().stream().anyMatch(s -> student.getEmail().equalsIgnoreCase(s.getEmail()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        }
        if (studentRepository.findAll().stream().anyMatch(s -> student.getStudentNumber().equalsIgnoreCase(s.getStudentNumber()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student number already exists.");
        }
        return studentRepository.save(student);
    }

    public Student updateStudent(Long id, Student updatedStudent) {
        Student student = getStudentById(id);

        if (updatedStudent.getName() != null && !updatedStudent.getName().isBlank()) {
            student.setName(updatedStudent.getName().trim());
        }
        if (updatedStudent.getEmail() != null && !updatedStudent.getEmail().isBlank()) {
            String newEmail = updatedStudent.getEmail().trim();
            if (studentRepository.findAll().stream().anyMatch(s -> !s.getId().equals(id) && newEmail.equalsIgnoreCase(s.getEmail()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
            }
            student.setEmail(newEmail);
        }
        if (updatedStudent.getStudentNumber() != null && !updatedStudent.getStudentNumber().isBlank()) {
            String newNumber = updatedStudent.getStudentNumber().trim();
            if (studentRepository.findAll().stream().anyMatch(s -> !s.getId().equals(id) && newNumber.equalsIgnoreCase(s.getStudentNumber()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Student number already exists.");
            }
            student.setStudentNumber(newNumber);
        }
        if (updatedStudent.getDepartment() != null && !updatedStudent.getDepartment().isBlank()) {
            student.setDepartment(updatedStudent.getDepartment().trim());
        }
        if (updatedStudent.getUsername() != null && !updatedStudent.getUsername().isBlank()) {
            student.setUsername(updatedStudent.getUsername().trim());
        }

        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        Student student = getStudentById(id);
        if (student.getUsername() != null && !student.getUsername().isBlank()) {
            userRepository.findByUsername(student.getUsername()).ifPresent(userRepository::delete);
        }
        studentRepository.delete(student);
    }
}
