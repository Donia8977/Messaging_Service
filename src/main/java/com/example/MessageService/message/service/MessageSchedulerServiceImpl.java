package com.example.MessageService.message.service;

import com.example.MessageService.Logging.service.MessageLogService;
import com.example.MessageService.message.MessagingSystem.MessageProducer;
import com.example.MessageService.message.dto.MessageSchedulerDto;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.entity.MessageStatus;
import com.example.MessageService.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageSchedulerServiceImpl implements MessageSchedulerService {

    private final MessageRepository messageRepository;
    private final MessageProducer messageProducer;
    private final MessageLogService messageLogService;



    @Scheduled(fixedRateString = "${messaging.scheduler.rate.ms}")
    @Transactional
    public void processScheduledMessages() {
        log.info("Running scheduler to process due messages...");

        // 1. Find all messages that are scheduled and due to be sent now.
        List<Message> dueMessages = messageRepository.findDueScheduledMessages(MessageStatus.SCHEDULED, LocalDateTime.now());

        if (dueMessages.isEmpty()) {
            log.info("No scheduled messages are due at this time.");
            return;
        }

        log.info("Found {} scheduled messages to process.", dueMessages.size());

        // 2. Process each due message.
        for (Message message : dueMessages) {
            try {
                // 3. IMPORTANT: Update status to PENDING immediately.
                // This acts as a lock to prevent other scheduler instances (in a clustered environment)
                // or the next run of this scheduler from picking up the same message again.
                message.setStatus(MessageStatus.PENDING);
                messageRepository.save(message);

                // 4. Send the message to the configured message broker (Kafka or NATS).
                messageProducer.sendMessage(message);

                // 5. Log the transition.
                messageLogService.createLog(message, MessageStatus.PENDING, "Message picked up by scheduler and sent to processing queue.");
                log.info("Message ID {} has been processed and sent to the message producer.", message.getId());

            } catch (Exception e) {
                // If something goes wrong trying to send to the broker, log it and move on.
                // The message will remain in a PENDING state and might require manual intervention.
                log.error("Failed to process scheduled message ID {}. Error: {}", message.getId(), e.getMessage(), e);
                // Optionally, you could set its status to a new "SCHEDULING_FAILED" state here.
            }
        }
    }
}