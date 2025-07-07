package com.example.MessageService.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}