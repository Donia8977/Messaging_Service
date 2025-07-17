package com.example.MessageService.message.MessageBroker.provider;

import com.example.MessageService.message.entity.Message;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("twilioWhatsAppProvider")
@Slf4j
public class WhatsAppProviderImpl implements WhatsAppProvider {

    @Value("${twilio.account.sid}")
    private String accountSid;
    @Value("${twilio.auth.token}")
    private String authToken;
    @Value("${twilio.whatsapp.from}")
    private String fromPhoneNumber;


    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio SDK initialized successfully for Account SID: {}", accountSid);
    }

    @Override
    public void send(Message message) {

        PhoneNumber to = new PhoneNumber("whatsapp:" + message.getUser().getPhone());
        PhoneNumber from = new PhoneNumber("whatsapp:" + fromPhoneNumber);
        String body = message.getContent();

        try {
            log.info("Attempting to send WhatsApp message from {} to {} with body: '{}'", from, to, body);
            com.twilio.rest.api.v2010.account.Message.creator(to, from, body).create();
            log.info("WhatsApp message sent successfully to {}", to);
        } catch (ApiException e) {
            log.error("Failed to send WhatsApp message via Twilio to {}. API Error: {} - {}", to, e.getCode(), e.getMessage());
            throw new RuntimeException("Failed to send WhatsApp message", e);
        }
    }
}