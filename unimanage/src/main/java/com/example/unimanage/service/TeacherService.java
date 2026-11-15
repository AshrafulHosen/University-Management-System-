package com.example.unimanage.service;

import com.example.unimanage.entity.Teacher;
import com.example.unimanage.repository.TeacherRepository;
import com.example.unimanage.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;

    public TeacherService(TeacherRepository teacherRepository, UserRepository userRepository) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Teacher getTeacherById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Teacher not found"));
    }

    @Transactional(readOnly = true)
    public Teacher getTeacherByUsername(String username) {
        return teacherRepository.findByUsername(username)
                .or(() -> teacherRepository.findByEmail(username))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Teacher record not found for username: " + username));
    }

    public Teacher createTeacher(Teacher teacher) {
        if (teacher.getName() == null || teacher.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required.");
        }
        if (teacher.getEmail() == null || teacher.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required.");
        }
        if (teacherRepository.findAll().stream().anyMatch(t -> teacher.getEmail().equalsIgnoreCase(t.getEmail()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        }
        return teacherRepository.save(teacher);
    }

    public Teacher updateTeacher(Long id, Teacher updatedTeacher) {
        Teacher teacher = getTeacherById(id);

        if (updatedTeacher.getName() != null && !updatedTeacher.getName().isBlank()) {
            teacher.setName(updatedTeacher.getName().trim());
        }
        if (updatedTeacher.getEmail() != null && !updatedTeacher.getEmail().isBlank()) {
            String newEmail = updatedTeacher.getEmail().trim();
            if (teacherRepository.findAll().stream().anyMatch(t -> !t.getId().equals(id) && newEmail.equalsIgnoreCase(t.getEmail()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
            }
            teacher.setEmail(newEmail);
        }
        if (updatedTeacher.getDepartment() != null && !updatedTeacher.getDepartment().isBlank()) {
            teacher.setDepartment(updatedTeacher.getDepartment().trim());
        }
        if (updatedTeacher.getUsername() != null && !updatedTeacher.getUsername().isBlank()) {
            teacher.setUsername(updatedTeacher.getUsername().trim());
        }

        return teacherRepository.save(teacher);
    }

    public void deleteTeacher(Long id) {
        Teacher teacher = getTeacherById(id);
        if (teacher.getUsername() != null && !teacher.getUsername().isBlank()) {
            userRepository.findByUsername(teacher.getUsername()).ifPresent(userRepository::delete);
        }
        teacherRepository.delete(teacher);
    }
}
