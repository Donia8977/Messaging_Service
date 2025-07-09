package com.example.MessageService.message.repository;

import com.example.MessageService.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message , Long> { }
