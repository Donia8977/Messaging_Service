package com.example.MessageService.message.MessagingSystem.provider;

import com.example.MessageService.message.dto.MessageSchedulerDto;


import com.example.MessageService.message.entity.EmailDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.MailException;


@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {


    private final EmailProviderImpl emailProviderImpl;

    @PostMapping("/send-direct")
    public ResponseEntity<String> sendDirectEmail(@Valid @RequestBody EmailDetails emailDetails) {
        try {

            emailProviderImpl.sendDirectEmail(emailDetails);
            return ResponseEntity.ok("Direct email sent successfully.");

        } catch (MailException e) {

            log.error("Mail operation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending email: " + e.getMessage());
        } catch (Exception e) {

            log.error("An unexpected error occurred during direct email sending: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected server error occurred.");
        }
    }
}


