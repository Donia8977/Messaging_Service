package com.example.MessageService.Logging.entity;

import com.example.MessageService.message.entity.Message;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;



    //Log Details
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Lob
    @Column(name = "channel_response", columnDefinition = "TEXT")
    private String channelResponse;

    @Lob
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;




    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}