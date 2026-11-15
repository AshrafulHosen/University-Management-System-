package com.example.unimanage.unit;

import com.example.unimanage.entity.Admin;
import com.example.unimanage.entity.Role;
import com.example.unimanage.entity.Student;
import com.example.unimanage.entity.Teacher;
import com.example.unimanage.entity.User;
import com.example.unimanage.repository.AdminRepository;
import com.example.unimanage.repository.StudentRepository;
import com.example.unimanage.repository.TeacherRepository;
import com.example.unimanage.repository.UserRepository;
import com.example.unimanage.service.AdminService;
import com.example.unimanage.service.StudentService;
import com.example.unimanage.service.TeacherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UniversityServiceUnitTests {

    // Mocks for AdminService
    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private AdminService adminService;

    // Mocks for TeacherService
    @Mock
    private TeacherRepository teacherRepository;

    // Mocks for StudentService
    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeacherService teacherService;

    @InjectMocks
    private StudentService studentService;

    // =========================================================================
    // UNIT TEST 1: AdminService - Create Admin Success
    // =========================================================================
    @Test
    @DisplayName("Unit Test 1: AdminService.createAdmin should save and return new admin when input is valid")
    void unitTest1_createAdmin_Success() {
        Admin admin = new Admin("Super Admin", "admin@unimanage.edu");
        when(adminRepository.findAll()).thenReturn(Collections.emptyList());
        when(adminRepository.save(admin)).thenReturn(admin);

        Admin result = adminService.createAdmin(admin);

        assertNotNull(result);
        assertEquals("Super Admin", result.getName());
        assertEquals("admin@unimanage.edu", result.getEmail());
        verify(adminRepository, times(1)).save(admin);
    }

    // =========================================================================
    // UNIT TEST 2: AdminService - Create Admin Throws Conflict on Duplicate Email
    // =========================================================================
    @Test
    @DisplayName("Unit Test 2: AdminService.createAdmin should throw 409 Conflict when email already exists")
    void unitTest2_createAdmin_ThrowsConflictWhenEmailExists() {
        Admin existing = new Admin("Existing Admin", "duplicate@unimanage.edu");
        Admin newAdmin = new Admin("New Admin", "duplicate@unimanage.edu");
        when(adminRepository.findAll()).thenReturn(List.of(existing));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> adminService.createAdmin(newAdmin));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(adminRepository, never()).save(any());
    }

    // =========================================================================
    // UNIT TEST 3: TeacherService - Create Teacher Success
    // =========================================================================
    @Test
    @DisplayName("Unit Test 3: TeacherService.createTeacher should save and return teacher when input is valid")
    void unitTest3_createTeacher_Success() {
        Teacher teacher = new Teacher("prof_turing", "Alan Turing", "turing@unimanage.edu", "Computer Science");
        when(teacherRepository.findAll()).thenReturn(Collections.emptyList());
        when(teacherRepository.save(teacher)).thenReturn(teacher);

        Teacher result = teacherService.createTeacher(teacher);

        assertNotNull(result);
        assertEquals("Alan Turing", result.getName());
        assertEquals("Computer Science", result.getDepartment());
        verify(teacherRepository, times(1)).save(teacher);
    }

    // =========================================================================
    // UNIT TEST 4: TeacherService - Update Teacher Success
    // =========================================================================
    @Test
    @DisplayName("Unit Test 4: TeacherService.updateTeacher should update fields and persist changes")
    void unitTest4_updateTeacher_Success() {
        Teacher existing = new Teacher("prof_curie", "Marie Curie", "marie@unimanage.edu", "Physics");
        existing.setId(10L);

        Teacher updatePayload = new Teacher(null, "Dr. Marie Curie", "marie.updated@unimanage.edu", "Nuclear Physics");

        when(teacherRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(teacherRepository.findAll()).thenReturn(List.of(existing));
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(inv -> inv.getArgument(0));

        Teacher updated = teacherService.updateTeacher(10L, updatePayload);

        assertNotNull(updated);
        assertEquals("Dr. Marie Curie", updated.getName());
        assertEquals("marie.updated@unimanage.edu", updated.getEmail());
        assertEquals("Nuclear Physics", updated.getDepartment());
        verify(teacherRepository, times(1)).save(existing);
    }

    // =========================================================================
    // UNIT TEST 5: StudentService - Create Student Success
    // =========================================================================
    @Test
    @DisplayName("Unit Test 5: StudentService.createStudent should save and return student when input is valid")
    void unitTest5_createStudent_Success() {
        Student student = new Student("grace_h", "Grace Hopper", "grace@unimanage.edu", "CS-2026-001", "Computer Science");
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());
        when(studentRepository.save(student)).thenReturn(student);

        Student result = studentService.createStudent(student);

        assertNotNull(result);
        assertEquals("Grace Hopper", result.getName());
        assertEquals("CS-2026-001", result.getStudentNumber());
        verify(studentRepository, times(1)).save(student);
    }

    // =========================================================================
    // UNIT TEST 6: StudentService - Delete Student Cleans Up Associated User
    // =========================================================================
    @Test
    @DisplayName("Unit Test 6: StudentService.deleteStudent should remove student and clean up linked user account")
    void unitTest6_deleteStudent_SuccessAndCleansUpAssociatedUser() {
        Student student = new Student("student_alif", "Alif", "alif@unimanage.edu", "STU-2207042", "CSE");
        student.setId(42L);
        User linkedUser = new User("student_alif", "hashedPassword", Role.STUDENT);

        when(studentRepository.findById(42L)).thenReturn(Optional.of(student));
        when(userRepository.findByUsername("student_alif")).thenReturn(Optional.of(linkedUser));

        studentService.deleteStudent(42L);

        verify(userRepository, times(1)).delete(linkedUser);
        verify(studentRepository, times(1)).delete(student);
    }
}
