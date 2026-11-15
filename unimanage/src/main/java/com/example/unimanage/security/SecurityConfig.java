package com.example.unimanage.security;

import com.example.unimanage.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomUserDetailsService userDetailsService)
            throws Exception {

        http
                .userDetailsService(userDetailsService)

                // Disable CSRF for REST API endpoints
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(request -> request.getRequestURI().startsWith("/api/"))
                )

                .authorizeHttpRequests(auth -> auth
                        // Public assets, login, and registration endpoints
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/api/auth/**",
                                "/style.css",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()

                        // Dashboard web page authorizations
                        .requestMatchers("/admin-dashboard").hasRole("ADMIN")
                        .requestMatchers("/teacher-dashboard").hasRole("TEACHER")
                        .requestMatchers("/student-dashboard").hasRole("STUDENT")

                        // REST API Role-Based Access Control (RBAC):
                        // Admins can manage all three resources
                        // Teachers can manage only teachers
                        // Students can manage only students
                        .requestMatchers("/api/admins/**").hasRole("ADMIN")
                        .requestMatchers("/api/teachers/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers("/api/students/**").hasAnyRole("ADMIN", "STUDENT")

                        .anyRequest().authenticated()
                )

                // Enable HTTP Basic authentication for REST API testing
                .httpBasic(Customizer.withDefaults())

                // Custom error response for unauthenticated REST API requests (401 instead of redirect to /login)
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                request -> request.getRequestURI().startsWith("/api/")
                        )
                )

                // Form login for web UI
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )

                // Logout configuration
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}
