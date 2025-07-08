package com.example.MessageService.security.service;

import com.example.MessageService.security.dto.CreateUserRequestDTO;
import com.example.MessageService.security.dto.UserResponseDTO;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.entity.User;
import com.example.MessageService.security.entity.UserPreferredChannel;
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

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setCity(dto.getCity());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setTenant(tenant);
        user.setType(dto.getUserType());
        user.setGender(dto.getGender());

        // Create UserPreferredChannel entities from ChannelType list
        List<UserPreferredChannel> preferredChannels = dto.getPreferredChannels().stream()
                .map(channelType -> {
                    UserPreferredChannel upc = new UserPreferredChannel();
                    upc.setChannelType(channelType);
                    upc.setUser(user); // Set user reference
                    return upc;
                }).collect(Collectors.toList());

        user.setPreferredChannels(preferredChannels);

        User saved = userRepo.save(user);

        return new UserResponseDTO(
                saved.getId(),
                saved.getUsername(),
                saved.getPhone(),
                saved.getEmail(),
                saved.getCity(),
                saved.getCreatedAt(),
                saved.getType(),
                saved.getPreferredChannels()
                        .stream()
                        .map(pc -> pc.getChannelType())
                        .toList(),
                saved.getGender(),
                saved.getTenant().getName()
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
                        u.getCreatedAt(),
                        u.getType(),
                        u.getPreferredChannels()
                                .stream()
                                .map(pc -> pc.getChannelType())
                                .toList(),
                        u.getGender(),
                        u.getTenant().getName()
                ))
                .collect(Collectors.toList());
    }
}
