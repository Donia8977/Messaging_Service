package com.example.MessageService.message.MessageBroker.kafka.producer;

import com.example.MessageService.message.MessageBroker.MessageProducer;
import com.example.MessageService.message.dto.MessageSchedulerDto;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.entity.Priority;
import com.example.MessageService.message.mapper.MessageMapper;
import com.example.MessageService.security.entity.ChannelType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Profile("kafka")
@AllArgsConstructor
public class KafkaMessageProducerImpl implements MessageProducer {

    private final KafkaTemplate<String, MessageSchedulerDto> kafkaTemplate;
    private final MessageMapper messageMapper;


    @Override
    public void sendMessage(Message message) {
        if (message == null || message.getTenant() == null) {
            log.error("Cannot send a null message or a message without a tenant. Aborting.");
            return;
        }

        String topic = getTopicFor(message.getChannel(), message.getPriority());
        String key = message.getTenant().getId().toString();

        try {
            MessageSchedulerDto dto = messageMapper.toDto(message);

            log.info("Sending message to topic [{}] for tenantId [{}], channel [{}], targetId [{}]",
                    topic, key, dto.getChannel(), dto.getTargetId());

            kafkaTemplate.send(topic, key, dto);

        } catch (Exception e) {
            log.error("Failed to send message with key [{}] to topic [{}]. Error: {}", key, topic, e.getMessage());
        }
    }



    private String getTopicFor(ChannelType channel, Priority priority) {
        String channelLower = channel.name().toLowerCase();
        String priorityString = (priority == Priority.HIGH) ? "high-priority" : "standard-priority";
        return String.format("%s-%s-topic", channelLower, priorityString);
    }
}