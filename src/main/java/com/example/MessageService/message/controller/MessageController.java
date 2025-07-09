package com.example.MessageService.message.controller;

import com.example.MessageService.message.dto.MessageSchedulerDto;
import com.example.MessageService.message.service.MessageSchedulerService;
import com.example.MessageService.message.service.MessageSchedulerServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageSchedulerService messageSchedulerService;


    @PostMapping("/api/messages")
    public ResponseEntity<Void> requestMessageCreation(@Valid @RequestBody MessageSchedulerDto request) {
        messageSchedulerService.processMessageRequest(request);
        return ResponseEntity.accepted().build();
    }

}
