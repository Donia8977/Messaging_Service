package com.example.MessageService.message.service;

import com.example.MessageService.Logging.service.MessageLogService;
import com.example.MessageService.message.dto.MessageSchedulerDto;
import com.example.MessageService.message.dto.TargetType;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.entity.MessageStatus;
import com.example.MessageService.message.MessageBroker.MessageProducer;
import com.example.MessageService.message.mapper.MessageMapper;
import com.example.MessageService.message.repository.MessageRepository;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.entity.User;
import com.example.MessageService.security.repository.TenantRepository;
import com.example.MessageService.security.repository.UserRepository;
import com.example.MessageService.segment.entity.Segment;
import com.example.MessageService.segment.repository.SegmentRepository;
import com.example.MessageService.template.entity.Template;
import com.example.MessageService.template.repository.TemplateRepository;
import com.example.MessageService.template.service.FieldExtractorUtil;
import com.example.MessageService.template.service.TemplateService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final TemplateRepository templateRepository;
    private final TemplateService templateService;
    private final MessageProducer messageProducer;
    private final MessageMapper messageMapper;
    private final MessageLogService messageLogService;
    private final FieldExtractorUtil fieldExtractorUtil;
    private final SegmentRepository segmentRepository;

    @Override
    @Transactional
    public void processAndRouteMessage(MessageSchedulerDto requestDto) {

        if (requestDto.getTargetType() == TargetType.SEGMENT) {
            processSegmentMessage(requestDto);
        } else {
            processUserMessage(requestDto);
        }
    }

    private void processUserMessage(MessageSchedulerDto requestDto) {
        log.info("Processing USER message for tenant {} and target {}.",
                requestDto.getTenantId(), requestDto.getTargetId());
        Message completeMessage = buildCompleteMessageFromDto(requestDto);
        saveAndRouteMessage(completeMessage);
    }

    private void processSegmentMessage(MessageSchedulerDto requestDto) {
        log.info("Processing SEGMENT message for tenant {} and segment {}.",
                requestDto.getTenantId(), requestDto.getTargetId());

        Segment segment = segmentRepository.findById(requestDto.getTargetId())
                .orElseThrow(() -> new EntityNotFoundException("Segment not found with id: " + requestDto.getTargetId()));

        List<User> targetUsers = List.copyOf(segment.getUsers());
        if (targetUsers.isEmpty()) {
            log.warn("Segment {} contains no users. Aborting.", segment.getId());
            return;
        }

        Template template = templateRepository.findById(requestDto.getTemplateId())
                .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + requestDto.getTemplateId()));

        log.info("Segment {} contains {} users. Creating a message for each.", segment.getId(), targetUsers.size());

        for (User user : targetUsers) {
            Message message = new Message();
            message.setTenant(segment.getTenant());
            message.setUser(user);
            message.setSegment(segment);
            message.setTemplate(template);
            message.setChannel(requestDto.getChannel());
            message.setPriority(requestDto.getPriority());
            message.setScheduledAt(requestDto.getScheduledAt());
            message.setCronExpression(requestDto.getCronExpression());
            message.setCreatedAt(LocalDateTime.now());

            String renderedContent = templateService.renderTemplate(template.getContent(), fieldExtractorUtil.extractFieldsAsMap(user));
            message.setContent(renderedContent);

            saveAndRouteMessage(message);
        }
    }


    private void saveAndRouteMessage(Message message) {
        // Case 1: The message is recurring (a CRON expression is provided)
        if (message.getCronExpression() != null && CronExpression.isValidExpression(message.getCronExpression())) {
            log.info("Saving RECURRING message template for user {}.", message.getUser().getId());

            message.setStatus(MessageStatus.RECURRING);

            // Calculate the *first* execution time for this specific recurring message.
            CronExpression cron = CronExpression.parse(message.getCronExpression());
            LocalDateTime firstExecutionTime = cron.next(LocalDateTime.now());
            message.setScheduledAt(firstExecutionTime);

            Message savedTemplate = messageRepository.save(message);
            messageLogService.createLog(savedTemplate, MessageStatus.RECURRING, "Recurring message template created. First instance for user " + message.getUser().getId() + " scheduled for: " + firstExecutionTime);

            // Case 2: The message is a one-time scheduled event
        } else if (message.getScheduledAt() != null) {
            log.info("Saving one-time SCHEDULED message for user {}.", message.getUser().getId());
            message.setStatus(MessageStatus.SCHEDULED);
            Message savedMessage = messageRepository.save(message);
            messageLogService.createLog(savedMessage, MessageStatus.SCHEDULED, "Message for user " + message.getUser().getId() + " accepted and scheduled for future delivery.");

            // Case 3: The message is immediate
        } else {
            log.info("Processing IMMEDIATE message for user {}.", message.getUser().getId());
            message.setStatus(MessageStatus.PENDING);
            Message savedMessage = messageRepository.save(message);
            messageLogService.createLog(savedMessage, MessageStatus.PENDING, "Message sent to processing queue.");
            messageProducer.sendMessage(savedMessage);
        }
    }


    private Message buildCompleteMessageFromDto(MessageSchedulerDto dto) {

        User user = userRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + dto.getTargetId()));

        Tenant tenant = tenantRepository.findById(dto.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with id: " + dto.getTenantId()));


        Message message = messageMapper.toEntity(dto);
        if (dto.getTemplateId() != null) {
            log.debug("Template ID provided: {}", dto.getTemplateId());
            Template template = templateRepository.findById(dto.getTemplateId())
                    .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + dto.getTemplateId()));

            log.debug("Rendering template {} for user {}", template.getId(), user.getId());
            String renderedContent = templateService.renderTemplate(template.getContent(), fieldExtractorUtil.extractFieldsAsMap(user));

            message.setTemplate(template);
            message.setContent(renderedContent);

        } else {
            log.debug("No template ID provided, using content directly.");
        }

        message.setUser(user);
        message.setTenant(tenant);
        message.setCreatedAt(LocalDateTime.now());
        log.debug("Successfully built complete message object for {}", tenant.getName());
        return message;
    }
}