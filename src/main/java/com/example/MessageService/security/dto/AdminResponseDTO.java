package com.example.MessageService.security.dto;

import com.example.MessageService.security.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private UserRole role;

}
