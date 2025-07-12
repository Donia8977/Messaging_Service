package com.example.MessageService.message.MessagingSystem.provider;

import com.example.MessageService.message.entity.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor; // Use RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service("smtpEmailProvider")
@Slf4j
@RequiredArgsConstructor
public class EmailProviderImpl implements EmailProvider {

    private final JavaMailSender mailSender;

    @Override
    public void send(Message message) {

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String from = message.getTenant().getEmail();
            String to = message.getUser().getEmail();

            String subject = message.getTemplate() != null ? message.getTemplate().getName() : "Notification from " + message.getTenant().getName();

            String body = message.getContent();

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(mimeMessage);
            log.info("Email sent successfully via SMTP to {}", to);

        } catch (MessagingException e) {
            log.error("A messaging exception occurred while sending email to {}: {}", message.getUser().getEmail(), e.getMessage());
            throw new RuntimeException("Failed to construct or send email message", e);
        }
    }
}