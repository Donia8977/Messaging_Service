package com.example.MessageService.security.service;

import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final TenantRepository repo;
    public CustomUserDetailsService(TenantRepository repo) { this.repo = repo; }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Tenant t = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Tenant not found: " + email));

        return User.builder()
                .username(t.getEmail())
                .password(t.getPassword())
                .authorities("ROLE_TENANT")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
