package com.example.MessageService.message.repository;

import com.example.MessageService.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepositoryWeb extends JpaRepository<Message, Long> {
    List<Message> findByUserId(Long userId);
}