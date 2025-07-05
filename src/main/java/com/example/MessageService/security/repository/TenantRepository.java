package com.example.MessageService.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.MessageService.security.entity.Tenant;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByEmail(String email);
    boolean existsByEmail(String email);
}
