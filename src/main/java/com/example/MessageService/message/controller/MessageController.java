package com.example.MessageService.message.controller;

import com.example.MessageService.message.dto.MessageSchedulerDto;
import com.example.MessageService.message.service.MessageSchedulerService;
import com.example.MessageService.message.service.MessageSchedulerServiceImpl;
import com.example.MessageService.message.service.MessageService;
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

    private final MessageService messageService;


    @PostMapping("/request")
    public ResponseEntity<Void> requestMessageCreation(@Valid @RequestBody MessageSchedulerDto request) {
        messageService.processAndRouteMessage(request);
        return ResponseEntity.accepted().build();
    }

}
