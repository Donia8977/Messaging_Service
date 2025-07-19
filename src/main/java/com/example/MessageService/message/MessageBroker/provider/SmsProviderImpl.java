package com.example.MessageService.message.MessageBroker.provider;

import com.example.MessageService.message.entity.Message;
import com.example.MessageService.security.entity.ChannelType;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("twilioSmsProvider")
@Slf4j
public class SmsProviderImpl implements SmsProvider {

    // Note: The Twilio SDK is already initialized by the TwilioWhatsAppProviderImpl's
    // @PostConstruct method, so we don't need to do it again here.

    @Value("${twilio.sms.from}")
    private String fromSmsNumber;

    @Override
    public void send(Message message) {

        PhoneNumber to = new PhoneNumber(message.getUser().getPhone());
        PhoneNumber from = new PhoneNumber(fromSmsNumber);
        String body = message.getContent();

        try {
            log.info("Attempting to send SMS from {} to {}.", from, to);

            MessageCreator creator = com.twilio.rest.api.v2010.account.Message.creator(to, from, body);
            creator.create();

            log.info("SMS sent successfully to {}", to);
        } catch (ApiException e) {
            log.error("Failed to send SMS via Twilio to {}. API Error: {} - {}", to, e.getCode(), e.getMessage());
            throw e;
        }
    }


    @Override
    public ChannelType getSupportedChannel() {
        return ChannelType.SMS;
    }
}