package com.example.MessageService.message.repository;

import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.entity.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MessageRepository extends JpaRepository<Message , Long> {

    @Query("SELECT m FROM Message m WHERE m.status = :status AND m.scheduledAt <= :now")
    List<Message> findDueScheduledMessages(@Param("status") MessageStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT m.status as status, COUNT(m) as count FROM Message m GROUP BY m.status")
    List<Map<String, Object>> countByStatus();
}

