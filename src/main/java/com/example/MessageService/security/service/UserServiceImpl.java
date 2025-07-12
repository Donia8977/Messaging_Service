package com.example.MessageService.security.service;

import com.example.MessageService.security.dto.CreateUserRequestDTO;
import com.example.MessageService.security.dto.UserResponseDTO;
import com.example.MessageService.security.entity.ChannelType;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.entity.User;
import com.example.MessageService.security.entity.UserPreferredChannel;
import com.example.MessageService.security.repository.TenantRepository;
import com.example.MessageService.security.repository.UserPreferredChannelRepository; // <-- 1. IMPORT THIS
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
    private final UserPreferredChannelRepository preferredChannelRepo;

    public UserServiceImpl(UserRepository userRepo,
                           TenantRepository tenantRepo,
                           PasswordEncoder encoder,
                           UserPreferredChannelRepository preferredChannelRepo) {
        this.userRepo = userRepo;
        this.tenantRepo = tenantRepo;
        this.encoder = encoder;
        this.preferredChannelRepo = preferredChannelRepo;
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

        User savedUser = userRepo.save(user);

        List<UserPreferredChannel> preferredChannels = dto.getPreferredChannels().stream()
                .map(channelType -> {
                    UserPreferredChannel upc = new UserPreferredChannel();
                    upc.setChannelType(channelType);
                    upc.setUser(savedUser);
                    return upc;
                }).collect(Collectors.toList());

        List<UserPreferredChannel> savedChannels = preferredChannelRepo.saveAll(preferredChannels);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getPhone(),
                savedUser.getEmail(),
                savedUser.getCity(),
                savedUser.getCreatedAt(),
                savedUser.getType(),
                savedChannels.stream()
                        .map(UserPreferredChannel::getChannelType)
                        .toList(),
                savedUser.getGender(),
                savedUser.getTenant().getName()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByTenant(Long tenantId) {
        if (!tenantRepo.existsById(tenantId)) {
            throw new IllegalArgumentException("Tenant not found: " + tenantId);
        }

        List<User> users = userRepo.findByTenantId(tenantId);
        return users.stream()
                .map(u -> {
                    List<ChannelType> channels = preferredChannelRepo.findByUserId(u.getId()).stream()
                            .map(UserPreferredChannel::getChannelType)
                            .toList();

                    return new UserResponseDTO(
                            u.getId(),
                            u.getUsername(),
                            u.getPhone(),
                            u.getEmail(),
                            u.getCity(),
                            u.getCreatedAt(),
                            u.getType(),
                            channels,
                            u.getGender(),
                            u.getTenant().getName()
                    );
                })
                .collect(Collectors.toList());
    }
}