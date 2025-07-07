package com.example.MessageService.security.repository;

import com.example.MessageService.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByTenantId(Long tenantId);
}
