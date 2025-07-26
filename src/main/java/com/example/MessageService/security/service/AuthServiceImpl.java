package com.example.MessageService.security.service;

import com.example.MessageService.security.dto.*;
import com.example.MessageService.security.entity.UserRole;
import com.example.MessageService.security.exception.EmailAlreadyExistsException;
import com.example.MessageService.security.exception.TenantNotFoundException;
import com.example.MessageService.security.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.jwt.JwtUtil;


@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authManager;
    private final TenantRepository tenantRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    @Value("${security.admin.registration-key}")
    private String superAdminRegistrationKey;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authManager,
                           TenantRepository tenantRepo,
                           PasswordEncoder encoder,
                           JwtUtil jwtUtil) {
        this.authManager = authManager;
        this.tenantRepo = tenantRepo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public RegisterResponseDTO registerTenant(RegisterRequestDTO req) {
        if (tenantRepo.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyExistsException(req.getEmail());
        }

        Tenant t = new Tenant();
        t.setName(req.getName());
        t.setPhone(req.getPhone());
        t.setEmail(req.getEmail());
        t.setPassword(encoder.encode(req.getPassword()));

        String providedKey = req.getRegistrationKey();

        if (providedKey != null && providedKey.equals(superAdminRegistrationKey)) {
            if (tenantRepo.existsByRole(UserRole.SUPER_ADMIN)) {
                throw new IllegalStateException("A Super Admin account already exists. The Super Admin key can only be used once.");
            }
            t.setRole(UserRole.SUPER_ADMIN);
        } else {
            t.setRole(UserRole.TENANT);
        }
        Tenant saved = tenantRepo.save(t);
        return new RegisterResponseDTO(saved.getId(), saved.getEmail());
    }

    public JwtResponse loginTenant(LoginRequestDTO req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        String email = ((UserDetails) auth.getPrincipal()).getUsername();

        Tenant tenant = tenantRepo.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Tenant not found: " + email));

        String token = jwtUtil.generateToken(email, tenant.getRole().name() );



        return new JwtResponse(
                token,
                "Bearer",
                tenant.getId(),
                email

        );
    }

    public void deleteTenant(Long tenantId){
        Tenant tenant = tenantRepo.findById(tenantId).orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));

        tenantRepo.delete(tenant);
    }

    public void deleteAllTenants(){
        tenantRepo.deleteAll();
    }


}
