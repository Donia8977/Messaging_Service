package com.example.MessageService.security.repository;

import com.example.MessageService.security.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> , UserRepositoryCustom {
    List<User> findByTenantId(Long tenantId);

    @Query("SELECT DISTINCT u.city FROM User u WHERE u.tenant.id = :tenantId AND u.city IS NOT NULL AND u.city <> ''")
    List<String> findDistinctCitiesByTenantId(@Param("tenantId") Long tenantId);

}
