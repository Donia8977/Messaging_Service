package com.example.MessageService.message.service;

import com.example.MessageService.message.MessagingSystem.provider.Provider;
import com.example.MessageService.message.dto.MessageSchedulerDto;

import com.example.MessageService.message.entity.Message;
import com.example.MessageService.message.entity.MessageStatus;
import com.example.MessageService.message.MessagingSystem.MessageProducer;
import com.example.MessageService.message.mapper.MessageMapper;
import com.example.MessageService.message.repository.MessageRepository;
import com.example.MessageService.security.entity.ChannelType;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.entity.User;
import com.example.MessageService.security.repository.TenantRepository;
import com.example.MessageService.security.repository.UserRepository;
import com.example.MessageService.template.entity.Template;
import com.example.MessageService.template.repository.TemplateRepository;
import com.example.MessageService.template.service.TemplateService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    private final Provider emailProvider;

    @Override
    @Transactional
    public void processAndRouteMessage(MessageSchedulerDto requestDto) {
        log.info("Processing request for tenant {} and target {}. Building complete message entity first.",
                requestDto.getTenantId(), requestDto.getTargetId());

        Message completeMessage = buildCompleteMessageFromDto(requestDto);

        if (requestDto.getScheduledAt() != null || requestDto.getCronExpression() != null) {
            log.info("Message from {} is scheduled. Saving to database.", completeMessage.getTenant().getName());
            completeMessage.setStatus(MessageStatus.SCHEDULED);
            messageRepository.save(completeMessage);
        } else {
            log.info("Message from {} is immediate. Saving and sending to Kafka.", completeMessage.getTenant().getName());
            completeMessage.setStatus(MessageStatus.PENDING);
            Message savedMessage = messageRepository.save(completeMessage);

            if (completeMessage.getChannel() == ChannelType.EMAIL) {
                emailProvider.send(savedMessage);
            }
            // Send it to Kafka.
            messageProducer.sendMessage(savedMessage);
            log.info("Message ID {} successfully sent to Kafka producer.", savedMessage.getId());
        }
    }


    private Message buildCompleteMessageFromDto(MessageSchedulerDto dto) {
        User user = userRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + dto.getTargetId()));

        Tenant tenant = tenantRepository.findById(dto.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with id: " + dto.getTenantId()));

        Template template = templateRepository.findById(dto.getTemplateId())
                .orElseThrow(() -> new EntityNotFoundException("Template not found with id: " + dto.getTemplateId()));


        log.debug("Rendering template {} for user {}", template.getId(), user.getId());
        String renderedContent = templateService.renderTemplate(template.getContent(), user.getUsername());

        Message message = messageMapper.toEntity(dto);
        message.setUser(user);
        message.setTenant(tenant);
        message.setTemplate(template);
        message.setContent(renderedContent);
        message.setCreatedAt(LocalDateTime.now());
        log.debug("Successfully built complete message object for {}", tenant.getName());
        return message;
    }
}