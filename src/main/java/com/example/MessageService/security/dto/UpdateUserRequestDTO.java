package com.example.MessageService.security.dto;
import com.example.MessageService.security.entity.ChannelType;
import com.example.MessageService.security.entity.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class UpdateUserRequestDTO {
    @NotBlank(message = "Username is required")
    private String username;

    private String phone;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    private String city;


    private String password;

    @NotNull(message = "userType is required")
    private UserType userType;

    @NotNull(message = "you must add at least one preferred channel")
    private List<ChannelType> preferredChannels;

    private String gender;
}