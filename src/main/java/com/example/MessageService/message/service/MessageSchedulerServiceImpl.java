package com.example.MessageService.message.service;

import com.example.MessageService.Logging.service.MessageLogService;
import com.example.MessageService.message.MessageBroker.MessageProducer;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.entity.MessageStatus;
import com.example.MessageService.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageSchedulerServiceImpl {

    private final MessageRepository messageRepository;
    private final MessageProducer messageProducer;
    private final MessageLogService messageLogService;


    @Scheduled(fixedRateString = "${messaging.scheduler.rate.ms}")
    @Transactional
    public void processScheduledMessages() {
        log.info("Running scheduler to process ONE-TIME due messages...");
        List<Message> dueMessages = messageRepository.findDueScheduledMessages(MessageStatus.SCHEDULED, LocalDateTime.now());

        if (dueMessages.isEmpty()) {
            log.info("No one-time scheduled messages are due at this time.");
            return;
        }

        log.info("Found {} one-time scheduled messages to process.", dueMessages.size());
        for (Message message : dueMessages) {
            try {
                message.setStatus(MessageStatus.PENDING);
                messageRepository.save(message);

                messageProducer.sendMessage(message);

                messageLogService.createLog(message, MessageStatus.PENDING, "Message picked up by scheduler and sent to processing queue.");
                log.info("One-time message ID {} has been processed and sent to the message producer.", message.getId());

            } catch (Exception e) {
                log.error("Failed to process scheduled message ID {}. Error: {}", message.getId(), e.getMessage(), e);
            }
        }
    }


    @Scheduled(fixedRateString = "${messaging.scheduler.rate.ms}")
    @Transactional
    public void processRecurringMessages() {
        log.info("Running scheduler to process RECURRING message templates...");

        List<Message> dueTemplates = messageRepository.findByStatusAndScheduledAtLessThanEqual(MessageStatus.RECURRING, LocalDateTime.now());

        if (dueTemplates.isEmpty()) {
            log.info("No recurring messages are due to generate an instance.");
            return;
        }

        log.info("Found {} recurring templates to process.", dueTemplates.size());
        for (Message template : dueTemplates) {
            try {

                Message instance = createInstanceFromTemplate(template);
                messageRepository.save(instance);
                log.info("Created new scheduled instance (ID: {}) from recurring template (ID: {}).", instance.getId(), template.getId());

                CronExpression cron = CronExpression.parse(template.getCronExpression());
                LocalDateTime nextExecutionTime = cron.next(LocalDateTime.now());
                template.setScheduledAt(nextExecutionTime);
                messageRepository.save(template);
                log.info("Updated recurring template ID {} with next execution time: {}", template.getId(), nextExecutionTime);

            } catch (Exception e) {
                log.error("Failed to process recurring template ID {}. It will be retried on the next run. Error: {}", template.getId(), e.getMessage(), e);
            }
        }
    }


    private Message createInstanceFromTemplate(Message template) {
        Message instance = new Message();
        instance.setTenant(template.getTenant());
        instance.setUser(template.getUser());
        instance.setSegment(template.getSegment());
        instance.setTemplate(template.getTemplate());
        instance.setContent(template.getContent());
        instance.setPriority(template.getPriority());
        instance.setChannel(template.getChannel());


        instance.setStatus(MessageStatus.SCHEDULED);
        instance.setScheduledAt(LocalDateTime.now());

        return instance;
    }
}