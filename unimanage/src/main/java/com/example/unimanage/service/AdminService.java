package com.example.unimanage.service;

import com.example.unimanage.entity.Admin;
import com.example.unimanage.repository.AdminRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Transactional(readOnly = true)
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Admin getAdminById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Admin not found"));
    }

    public Admin createAdmin(Admin admin) {
        if (admin.getName() == null || admin.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required.");
        }
        if (admin.getEmail() == null || admin.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required.");
        }
        if (adminRepository.findAll().stream().anyMatch(a -> admin.getEmail().equalsIgnoreCase(a.getEmail()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        }
        return adminRepository.save(admin);
    }

    public Admin updateAdmin(Long id, Admin updatedAdmin) {
        Admin admin = getAdminById(id);

        if (updatedAdmin.getName() != null && !updatedAdmin.getName().isBlank()) {
            admin.setName(updatedAdmin.getName().trim());
        }
        if (updatedAdmin.getEmail() != null && !updatedAdmin.getEmail().isBlank()) {
            String newEmail = updatedAdmin.getEmail().trim();
            if (adminRepository.findAll().stream().anyMatch(a -> !a.getId().equals(id) && newEmail.equalsIgnoreCase(a.getEmail()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
            }
            admin.setEmail(newEmail);
        }

        return adminRepository.save(admin);
    }

    public void deleteAdmin(Long id) {
        Admin admin = getAdminById(id);
        adminRepository.delete(admin);
    }
}
