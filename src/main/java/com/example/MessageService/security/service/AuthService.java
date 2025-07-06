package com.example.MessageService.security.service;

import com.example.MessageService.security.dto.*;

public interface AuthService {
    RegisterResponseDTO registerTenant(RegisterRequestDTO req);
    JwtResponse loginTenant(LoginRequestDTO req);

}
