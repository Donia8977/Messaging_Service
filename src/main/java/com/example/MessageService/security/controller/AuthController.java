package com.example.MessageService.security.controller;
import com.example.MessageService.security.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.MessageService.security.service.AuthService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(
            @Validated @RequestBody RegisterRequestDTO req) {
        RegisterResponseDTO resp = authService.registerTenant(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }


    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Validated @RequestBody LoginRequestDTO req) {
        log.info("Login attempt for {}", req.getEmail());


        JwtResponse response = authService.loginTenant(req);
        return ResponseEntity.ok(response);

    }



}

