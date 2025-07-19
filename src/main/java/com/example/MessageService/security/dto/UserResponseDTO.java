package com.example.MessageService.security.dto;

import com.example.MessageService.security.entity.ChannelType;
import com.example.MessageService.security.entity.UserPreferredChannel;
import com.example.MessageService.security.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private String city;
    private LocalDateTime createdAt;
    private UserType userType;
    private List<ChannelType> preferredChannels;
    private String gender;
    private String tenantName;
}
