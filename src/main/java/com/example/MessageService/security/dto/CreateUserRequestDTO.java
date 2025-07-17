package com.example.MessageService.security.dto;

import com.example.MessageService.security.entity.ChannelType;
import com.example.MessageService.security.entity.UserPreferredChannel;
import com.example.MessageService.security.entity.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDTO {
    @NotBlank(message = "Username is required")
    private String username;

    private String phone;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    private String city;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "userType is required")
    private UserType userType;

    @NotNull(message = "A preferred channel must be selected")
    private ChannelType preferredChannel;


    private String gender;
}