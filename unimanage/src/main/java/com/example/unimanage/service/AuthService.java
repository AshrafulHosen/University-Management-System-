package com.example.unimanage.service;

import com.example.unimanage.dto.RegisterDto;
import com.example.unimanage.entity.Role;
import com.example.unimanage.entity.Student;
import com.example.unimanage.entity.Teacher;
import com.example.unimanage.entity.User;
import com.example.unimanage.repository.StudentRepository;
import com.example.unimanage.repository.TeacherRepository;
import com.example.unimanage.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            TeacherRepository teacherRepository,
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterDto dto) {
        // Validate required fields
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required.");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required.");
        }
        if (dto.getRole() == null || (dto.getRole() != Role.TEACHER && dto.getRole() != Role.STUDENT)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be either TEACHER or STUDENT.");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required.");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required.");
        }

        String username = dto.getUsername().trim();
        String email = dto.getEmail().trim();

        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
        }

        // Check if email already exists
        if (teacherRepository.findByEmail(email).isPresent() || studentRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered.");
        }

        // Create User entity with hashed password
        User user = new User(
                username,
                passwordEncoder.encode(dto.getPassword().trim()),
                dto.getRole()
        );
        userRepository.save(user);

        // Create role-specific entity
        if (dto.getRole() == Role.TEACHER) {
            String department = (dto.getDepartment() != null && !dto.getDepartment().isBlank())
                    ? dto.getDepartment().trim() : "General";
            Teacher teacher = new Teacher(username, dto.getName().trim(), email, department);
            teacherRepository.save(teacher);
        } else if (dto.getRole() == Role.STUDENT) {
            String department = (dto.getDepartment() != null && !dto.getDepartment().isBlank())
                    ? dto.getDepartment().trim() : "General";
            String studentNumber = (dto.getStudentNumber() != null && !dto.getStudentNumber().isBlank())
                    ? dto.getStudentNumber().trim() : "STU-" + System.currentTimeMillis() % 100000;

            // Check student number uniqueness
            if (studentRepository.findAll().stream().anyMatch(s -> studentNumber.equalsIgnoreCase(s.getStudentNumber()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Student Number is already in use.");
            }

            Student student = new Student(username, dto.getName().trim(), email, studentNumber, department);
            studentRepository.save(student);
        }
    }
}

