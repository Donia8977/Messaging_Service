package com.example.MessageService.security.controller;


import com.example.MessageService.security.dto.RegisterRequestDTO;
import com.example.MessageService.security.dto.RegisterResponseDTO;
import com.example.MessageService.security.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<RegisterResponseDTO> createAdmin(@RequestBody RegisterRequestDTO req) {
        RegisterResponseDTO resp = adminService.createAdmin(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}

