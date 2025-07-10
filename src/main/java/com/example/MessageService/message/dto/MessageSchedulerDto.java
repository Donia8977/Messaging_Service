package com.example.MessageService.message.dto;

import com.example.MessageService.message.entity.Priority;
import com.example.MessageService.security.entity.ChannelType;
import com.example.MessageService.security.entity.Tenant;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageSchedulerDto {

    @NotNull
    private Long tenantId;

    @NotNull(message = "targetType must be provided (e.g., USER, SEGMENT)")
    private TargetType targetType;

    @NotNull(message = "targetId must be provided")
    private Long targetId;

    @NotNull(message = "channel must be provided")
    private ChannelType channel;

    private Long templateId;

    private String content;

    @Future(message = "scheduledAt must be in the future")
    private LocalDateTime scheduledAt;

    @Size(max = 255)
    private String cronExpression;

    @NotNull
    private Priority priority = Priority.STANDARD;
    @NotNull
    private UUID idempotencyKey;
}