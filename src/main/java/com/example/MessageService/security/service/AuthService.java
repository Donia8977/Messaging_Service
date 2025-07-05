package com.example.MessageService.security.service;

import com.example.MessageService.security.dto.LoginRequestDTO;
import com.example.MessageService.security.dto.LoginResponseDTO;
import com.example.MessageService.security.dto.RegisterRequestDTO;
import com.example.MessageService.security.dto.RegisterResponseDTO;

public interface AuthService {
    RegisterResponseDTO registerTenant(RegisterRequestDTO req);
    LoginResponseDTO loginTenant(LoginRequestDTO req);
}
