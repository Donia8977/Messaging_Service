package com.example.MessageService.security.service;


import com.example.MessageService.security.dto.RegisterRequestDTO;
import com.example.MessageService.security.dto.RegisterResponseDTO;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.entity.UserRole;
import com.example.MessageService.security.exception.EmailAlreadyExistsException;
import com.example.MessageService.security.repository.TenantRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final TenantRepository tenantRepo;
    private final PasswordEncoder encoder;

    public AdminService(TenantRepository tenantRepo, PasswordEncoder encoder) {
        this.tenantRepo = tenantRepo;
        this.encoder = encoder;
    }

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
}
