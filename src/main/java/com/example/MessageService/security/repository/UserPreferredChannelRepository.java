package com.example.MessageService.security.repository;

import com.example.MessageService.security.entity.UserPreferredChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPreferredChannelRepository extends JpaRepository<UserPreferredChannel, Long> {
    List<UserPreferredChannel> findByUserId(Long userId);
}