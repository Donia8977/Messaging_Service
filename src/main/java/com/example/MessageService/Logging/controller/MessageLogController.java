package com.example.MessageService.Logging.controller;

import com.example.MessageService.Logging.dto.MessageLogResponse;
import com.example.MessageService.Logging.service.MessageLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MessageLogController {

    private final MessageLogService messageLogService;


    @GetMapping("/logging/{messageId}")
    public ResponseEntity<List<MessageLogResponse>> getLogsForMessage(@PathVariable Long messageId) {

//        if (!messageRepository.existsById(messageId)) {
//            return ResponseEntity.notFound().build(); //404
//        }

        List<MessageLogResponse> logs = messageLogService.findLogsByMessageId(messageId);

        return ResponseEntity.ok(logs);
    }
}