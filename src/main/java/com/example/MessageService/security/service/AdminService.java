package com.example.MessageService.security.service;


import com.example.MessageService.security.dto.AdminResponseDTO;
import com.example.MessageService.security.dto.RegisterRequestDTO;
import com.example.MessageService.security.dto.RegisterResponseDTO;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.entity.User;
import com.example.MessageService.security.entity.UserRole;
import com.example.MessageService.security.exception.EmailAlreadyExistsException;
import com.example.MessageService.security.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final TenantRepository tenantRepo;
    private final PasswordEncoder encoder;

    public AdminService(TenantRepository tenantRepo, PasswordEncoder encoder) {
        this.tenantRepo = tenantRepo;
        this.encoder = encoder;
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public RegisterResponseDTO createAdmin(RegisterRequestDTO req) {
        if (tenantRepo.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyExistsException(req.getEmail());
        }

        Tenant admin = new Tenant();
        admin.setName(req.getName());
        admin.setPhone(req.getPhone());
        admin.setEmail(req.getEmail());
        admin.setPassword(encoder.encode(req.getPassword()));
        admin.setRole(UserRole.ADMIN);

        Tenant saved = tenantRepo.save(admin);
        return new RegisterResponseDTO(saved.getId(), saved.getEmail());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void deleteAdmin(Long adminId) {

        Tenant adminToDelete = tenantRepo.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin with ID " + adminId + " not found."));

        String currentAdminEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        if (adminToDelete.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new IllegalArgumentException("Action forbidden: You cannot delete your own account.");
        }

        if (adminToDelete.getRole() == UserRole.SUPER_ADMIN) {
            throw new SecurityException("Action forbidden: Cannot delete another SUPER_ADMIN account.");
        }


        tenantRepo.delete(adminToDelete);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN' ,'ADMIN')")
    public List<AdminResponseDTO> getAllAdmins(){

        List<Tenant> admins = tenantRepo.findAll().stream()
                .filter(tenant -> tenant.getRole() == UserRole.ADMIN || tenant.getRole() == UserRole.SUPER_ADMIN)
                .collect(Collectors.toList());

        return admins.stream()
                .map(admin -> new AdminResponseDTO(
                        admin.getId(),
                        admin.getName(),
                        admin.getEmail(),
                        admin.getPhone(),
                        admin.getCreatedAt(),
                        admin.getRole()))
                .collect(Collectors.toList());
    }


    }

