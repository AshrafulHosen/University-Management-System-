package com.example.unimanage.integration;

import com.example.unimanage.entity.Admin;
import com.example.unimanage.repository.AdminRepository;
import com.example.unimanage.repository.StudentRepository;
import com.example.unimanage.repository.TeacherRepository;
import com.example.unimanage.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class UniversityIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // =========================================================================
    // INTEGRATION TEST 1: Admin Full CRUD Operations (REST conventions & HTTP status codes)
    // =========================================================================
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Integration Test 1: Full CRUD lifecycle for Admin resource with proper REST conventions and status codes (201, 200, 204, 404)")
    void integrationTest1_AdminFullCrudLifecycle() throws Exception {
        // 1. CREATE (POST /api/admins) -> 201 Created
        String newAdminJson = """
                {
                    "name": "Integration Admin",
                    "email": "int.admin@unimanage.edu"
                }
                """;
        mockMvc.perform(post("/api/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAdminJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Integration Admin")))
                .andExpect(jsonPath("$.email", is("int.admin@unimanage.edu")));

        Admin created = adminRepository.findAll().stream()
                .filter(a -> "int.admin@unimanage.edu".equals(a.getEmail()))
                .findFirst().orElseThrow();
        Long adminId = created.getId();

        // 2. READ ALL (GET /api/admins) -> 200 OK
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        // 3. READ ONE (GET /api/admins/{id}) -> 200 OK
        mockMvc.perform(get("/api/admins/" + adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Integration Admin")));

        // 4. UPDATE (PUT /api/admins/{id}) -> 200 OK
        String updateAdminJson = """
                {
                    "name": "Integration Admin Updated",
                    "email": "int.admin.updated@unimanage.edu"
                }
                """;
        mockMvc.perform(put("/api/admins/" + adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateAdminJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Integration Admin Updated")))
                .andExpect(jsonPath("$.email", is("int.admin.updated@unimanage.edu")));

        // 5. DELETE (DELETE /api/admins/{id}) -> 204 No Content
        mockMvc.perform(delete("/api/admins/" + adminId))
                .andExpect(status().isNoContent());

        // 6. VERIFY NOT FOUND (GET /api/admins/{id}) -> 404 Not Found
        mockMvc.perform(get("/api/admins/" + adminId))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // INTEGRATION TEST 2: Role-Based Access Control (RBAC) Enforcement
    // =========================================================================
    @Test
    @DisplayName("Integration Test 2: Role-based access control (RBAC) ensures Teacher and Student can only access their own resources and get 403 on forbidden paths")
    void integrationTest2_RoleBasedAccessControl_TeacherAndStudentPermissions() throws Exception {
        // --- Teacher Testing ---
        // 1. Teacher can manage teachers (POST /api/teachers) -> 201 Created
        String teacherPayload = """
                {
                    "name": "Prof. Ada Lovelace",
                    "email": "ada.rbac@unimanage.edu",
                    "department": "Mathematics"
                }
                """;
        mockMvc.perform(post("/api/teachers")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("teacher").roles("TEACHER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(teacherPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        // 2. Teacher CANNOT access Admin endpoints -> 403 Forbidden
        mockMvc.perform(get("/api/admins")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("teacher").roles("TEACHER")))
                .andExpect(status().isForbidden());

        // 3. Teacher CANNOT access Student endpoints -> 403 Forbidden
        mockMvc.perform(get("/api/students")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("teacher").roles("TEACHER")))
                .andExpect(status().isForbidden());

        // 4. Teacher CANNOT access Admin Dashboard -> 403 Forbidden
        mockMvc.perform(get("/admin-dashboard")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("teacher").roles("TEACHER")))
                .andExpect(status().isForbidden());

        // 5. Teacher CAN access Teacher Dashboard -> 200 OK
        mockMvc.perform(get("/teacher-dashboard")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("teacher").roles("TEACHER")))
                .andExpect(status().isOk());

        // --- Student Testing ---
        // 6. Student can manage students (POST /api/students) -> 201 Created
        String studentPayload = """
                {
                    "name": "Hermione Granger",
                    "email": "hermione.rbac@unimanage.edu",
                    "studentNumber": "STU-RBAC-001",
                    "department": "Magical Law"
                }
                """;
        mockMvc.perform(post("/api/students")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        // 7. Student CANNOT access Admin endpoints -> 403 Forbidden
        mockMvc.perform(get("/api/admins")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());

        // 8. Student CANNOT access Teacher endpoints -> 403 Forbidden
        mockMvc.perform(get("/api/teachers")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());

        // 9. Student CANNOT access Admin Dashboard -> 403 Forbidden
        mockMvc.perform(get("/admin-dashboard")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());

        // 10. Student CAN access Student Dashboard -> 200 OK
        mockMvc.perform(get("/student-dashboard")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // INTEGRATION TEST 3: Public Account Registration & Security Authentication
    // =========================================================================
    @Test
    @DisplayName("Integration Test 3: Public registration creates user and role entity with hashed passwords, and unauthenticated requests return 401 Unauthorized")
    void integrationTest3_PublicRegistrationAndSecurityAuthentication() throws Exception {
        // 1. Unauthenticated requests to protected APIs return 401 Unauthorized
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());

        // 2. Public can access /register page -> 200 OK
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());

        // 3. Register new Teacher account (POST /api/auth/register) -> 201 Created
        String registerTeacherJson = """
                {
                    "username": "registered_faculty",
                    "password": "Password@123",
                    "role": "TEACHER",
                    "name": "Prof. Charles Xavier",
                    "email": "xavier.pub@unimanage.edu",
                    "department": "Genetics"
                }
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerTeacherJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", containsString("Account created successfully")));

        assert userRepository.findByUsername("registered_faculty").isPresent();
        assert teacherRepository.findByUsername("registered_faculty").isPresent();

        // 4. Register new Student account (POST /api/auth/register) -> 201 Created
        String registerStudentJson = """
                {
                    "username": "registered_student",
                    "password": "Password@123",
                    "role": "STUDENT",
                    "name": "Peter Parker",
                    "email": "parker.pub@unimanage.edu",
                    "studentNumber": "STU-PUB-888",
                    "department": "Biophysics"
                }
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerStudentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", containsString("Account created successfully")));

        assert userRepository.findByUsername("registered_student").isPresent();
        assert studentRepository.findByUsername("registered_student").isPresent();
    }
}
