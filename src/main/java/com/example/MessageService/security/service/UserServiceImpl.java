package com.example.MessageService.security.service;

import com.example.MessageService.security.dto.CreateUserRequestDTO;
import com.example.MessageService.security.dto.UserResponseDTO;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.entity.User;
import com.example.MessageService.security.repository.TenantRepository;
import com.example.MessageService.security.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepo;
    private final TenantRepository tenantRepo;
    private final PasswordEncoder encoder;

    public UserServiceImpl(UserRepository userRepo,
                           TenantRepository tenantRepo,
                           PasswordEncoder encoder) {
        this.userRepo   = userRepo;
        this.tenantRepo = tenantRepo;
        this.encoder    = encoder;
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(Long tenantId, CreateUserRequestDTO dto) {
        Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        User u = new User();
        u.setUsername(dto.getUsername());
        u.setPhone(dto.getPhone());
        u.setEmail(dto.getEmail());
        u.setCity(dto.getCity());
        u.setPassword(encoder.encode(dto.getPassword()));
        u.setTenant(tenant);

        User saved = userRepo.save(u);
        return new UserResponseDTO(
                saved.getId(),
                saved.getUsername(),
                saved.getPhone(),
                saved.getEmail(),
                saved.getCity(),
                saved.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByTenant(Long tenantId) {
        if (!tenantRepo.existsById(tenantId)) {
            throw new IllegalArgumentException("Tenant not found: " + tenantId);
        }
        return userRepo.findByTenantId(tenantId).stream()
                .map(u -> new UserResponseDTO(
                        u.getId(),
                        u.getUsername(),
                        u.getPhone(),
                        u.getEmail(),
                        u.getCity(),
                        u.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
