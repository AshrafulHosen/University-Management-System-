package com.example.unimanage.config;

import com.example.unimanage.entity.Admin;
import com.example.unimanage.entity.Role;
import com.example.unimanage.entity.User;
import com.example.unimanage.repository.AdminRepository;
import com.example.unimanage.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            UserRepository userRepository,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // Seed Admin User
            if (userRepository.findByUsername("admin").isEmpty()) {
                userRepository.save(new User(
                        "admin",
                        passwordEncoder.encode("Admin@123"),
                        Role.ADMIN
                ));
                System.out.println("Default ADMIN account created: admin / Admin@123");
            }

            // Seed Teacher User
            if (userRepository.findByUsername("teacher").isEmpty()) {
                userRepository.save(new User(
                        "teacher",
                        passwordEncoder.encode("Teacher@123"),
                        Role.TEACHER
                ));
                System.out.println("Default TEACHER account created: teacher / Teacher@123");
            }

            // Seed Student User
            if (userRepository.findByUsername("student").isEmpty()) {
                userRepository.save(new User(
                        "student",
                        passwordEncoder.encode("Student@123"),
                        Role.STUDENT
                ));
                System.out.println("Default STUDENT account created: student / Student@123");
            }

            // Seed initial Admin resource
            if (adminRepository.count() == 0) {
                adminRepository.save(new Admin("System Administrator", "admin@unimanage.edu"));
            }
        };
    }
}
