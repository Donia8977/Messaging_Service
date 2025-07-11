package com.example.MessageService.message.MessagingSystem.provider;

import com.example.MessageService.message.entity.EmailDetails;
import com.example.MessageService.message.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
//@Profile("Kafka")
@Slf4j
public class EmailProviderImpl implements Provider {

    private final JavaMailSender mailSender;


    public EmailProviderImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


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

    public void sendDirectEmail(EmailDetails details) {
        try {

            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(details.getTo());
            email.setSubject(details.getSubject());
            email.setText(details.getContent());

            mailSender.send(email);
            log.info("Direct email sent successfully to {}", details.getTo());
        } catch (Exception e) {
            log.error("Error sending direct email to {}: {}", details.getTo(), e.getMessage(), e);
            throw new MailAuthenticationException("Failed to send direct email", e);
        }
    }

}
