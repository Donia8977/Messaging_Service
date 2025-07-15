package com.example.MessageService.security.service;


import com.example.MessageService.security.dto.CreateUserRequestDTO;
import com.example.MessageService.security.dto.UserResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserService {
    UserResponseDTO createUser(Long tenantId, CreateUserRequestDTO dto);
    List<UserResponseDTO> getUsersByTenant(Long tenantId);
    void deleteUser(Long userId, Long tenantId);
    void deleteAllUsersByTenant(Long tenantId);
}


