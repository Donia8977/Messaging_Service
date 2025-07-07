package com.example.MessageService.Logging.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageLogResponse {
    private Long id;
    private Long messageId;
    private String status;
    private int attemptNumber;
    private String errorMessage;
    private LocalDateTime timestamp;
}
