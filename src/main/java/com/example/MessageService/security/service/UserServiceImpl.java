package com.example.MessageService.security.service;

import com.example.MessageService.exception.NotFoundException;
import com.example.MessageService.exception.UnauthorizedException;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.repository.MessageRepository;
import com.example.MessageService.message.repository.MessageRepositoryWeb;
import com.example.MessageService.security.dto.CreateUserRequestDTO;
import com.example.MessageService.security.dto.UpdateUserRequestDTO;
import com.example.MessageService.security.dto.UserResponseDTO;
import com.example.MessageService.security.entity.ChannelType;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.entity.User;
import com.example.MessageService.security.entity.UserPreferredChannel;
import com.example.MessageService.security.repository.TenantRepository;
import com.example.MessageService.security.repository.UserPreferredChannelRepository; // <-- 1. IMPORT THIS
import com.example.MessageService.security.repository.UserRepository;
import com.example.MessageService.segment.entity.Segment;
import com.example.MessageService.segment.repository.SegmentRepository;
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
    private final SegmentRepository segmentRepository;
    private final MessageRepositoryWeb messageRepositoryWeb;

    public UserServiceImpl(UserRepository userRepo,
                           TenantRepository tenantRepo,
                           PasswordEncoder encoder,
                           UserPreferredChannelRepository preferredChannelRepo, SegmentRepository segmentRepository, MessageRepository messageRepository, MessageRepositoryWeb messageRepositoryWeb) {
        this.userRepo = userRepo;
        this.tenantRepo = tenantRepo;
        this.encoder = encoder;
        this.preferredChannelRepo = preferredChannelRepo;
        this.segmentRepository = segmentRepository;

        this.messageRepositoryWeb = messageRepositoryWeb;
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

        preferredChannelRepo.deleteAllInBatch(preferredChannelRepo.findByUserId(savedUser.getId()));

        UserPreferredChannel upc = new UserPreferredChannel();
        upc.setChannelType(dto.getPreferredChannel());
        upc.setUser(savedUser);
        UserPreferredChannel savedChannel = preferredChannelRepo.save(upc);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getPhone(),
                savedUser.getEmail(),
                savedUser.getCity(),
                savedUser.getCreatedAt(),
                savedUser.getType(),
                List.of(savedChannel.getChannelType()),
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

    @Override
    @Transactional
    public void deleteUser(Long userId, Long tenantId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found."));

        if (!user.getTenant().getId().equals(tenantId)) {
            throw new UnauthorizedException("You are not authorized to delete this user.");
        }


        List<Segment> segmentsContainingUser = segmentRepository.findSegmentsByUserId(userId);
        if (!segmentsContainingUser.isEmpty()) {

            for (Segment segment : segmentsContainingUser) {
                segment.getUsers().remove(user);
            }
            segmentRepository.saveAll(segmentsContainingUser);
        }

        List<Message> messagesToUpdate = messageRepositoryWeb.findByUserId(userId);
        if (!messagesToUpdate.isEmpty()) {
            messagesToUpdate.forEach(message -> message.setUser(null));
            messageRepositoryWeb.saveAll(messagesToUpdate);
        }

        List<UserPreferredChannel> channels = preferredChannelRepo.findByUserId(userId);
        preferredChannelRepo.deleteAll(channels);

        userRepo.delete(user);
    }

    @Override
    @Transactional
    public void deleteAllUsersByTenant(Long tenantId) {
        List<User> usersToDelete = userRepo.findByTenantId(tenantId);
        if (usersToDelete.isEmpty()) {
            return;
        }

        List<Long> userIds = usersToDelete.stream().map(User::getId).collect(Collectors.toList());

        for(Long userId : userIds) {

            List<Segment> segmentsContainingUser = segmentRepository.findSegmentsByUserId(userId);
            if (!segmentsContainingUser.isEmpty()) {

                User userToRemove = usersToDelete.stream().filter(u -> u.getId().equals(userId)).findFirst().get();
                for (Segment segment : segmentsContainingUser) {
                    segment.getUsers().remove(userToRemove);
                }
                segmentRepository.saveAll(segmentsContainingUser);
            }

            List<Message> messagesToUpdate = messageRepositoryWeb.findByUserId(userId);
            if (!messagesToUpdate.isEmpty()) {
                messagesToUpdate.forEach(msg -> msg.setUser(null));
                messageRepositoryWeb.saveAll(messagesToUpdate);
            }


            List<UserPreferredChannel> channels = preferredChannelRepo.findByUserId(userId);
            if (!channels.isEmpty()) {
                preferredChannelRepo.deleteAll(channels);
            }
        }

        userRepo.deleteAll(usersToDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long userId, Long tenantId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        if (!user.getTenant().getId().equals(tenantId)) {
            throw new UnauthorizedException("You are not authorized to view this user.");
        }


        List<ChannelType> channels = preferredChannelRepo.findByUserId(user.getId()).stream()
                .map(UserPreferredChannel::getChannelType)
                .toList();

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getPhone(),
                user.getEmail(),
                user.getCity(),
                user.getCreatedAt(),
                user.getType(),
                channels,
                user.getGender(),
                user.getTenant().getName()
        );
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Long userId, Long tenantId, UpdateUserRequestDTO dto) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        if (!user.getTenant().getId().equals(tenantId)) {
            throw new UnauthorizedException("You are not authorized to edit this user.");
        }

        user.setUsername(dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setCity(dto.getCity());
        user.setType(dto.getUserType());
        user.setGender(dto.getGender());


        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(encoder.encode(dto.getPassword()));
        }

        preferredChannelRepo.deleteAllInBatch(preferredChannelRepo.findByUserId(userId));

        UserPreferredChannel upc = new UserPreferredChannel();
        upc.setChannelType(dto.getPreferredChannel());
        upc.setUser(user);
        UserPreferredChannel savedChannel = preferredChannelRepo.save(upc);

        User updatedUser = userRepo.save(user);

        return new UserResponseDTO(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getPhone(),
                updatedUser.getEmail(),
                updatedUser.getCity(),
                updatedUser.getCreatedAt(),
                updatedUser.getType(),
                List.of(savedChannel.getChannelType()),
                updatedUser.getGender(),
                updatedUser.getTenant().getName()
        );
    }
}