package com.example.MessageService.Logging.repository;
import com.example.MessageService.Logging.entity.MessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageLogRepository extends JpaRepository<MessageLog , Long> {
    List<MessageLog> findByMessageId(Long messageId);
}
