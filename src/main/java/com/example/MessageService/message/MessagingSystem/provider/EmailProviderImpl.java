package com.example.MessageService.message.MessagingSystem.provider;

import com.example.MessageService.message.entity.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class EmailProviderImpl implements EmailProvider {

    private final JavaMailSender mailSender;

    @Override
    public void send(Message message) {
        try {
            String from = message.getTenant().getEmail();
            String to = message.getUser().getEmail();
            String subject = "Your message from " + message.getTenant().getName();
            String body = message.getContent();

            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(from);
            email.setTo(to);
            email.setSubject(subject);
            email.setText(body);

            mailSender.send(email);
            log.info("Email sent from {} to {}", from, to);
        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage(), e);
        }
    }

}
