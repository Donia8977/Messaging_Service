package com.example.MessageService.message.dto;
import com.example.MessageService.message.entity.Priority;
import com.example.MessageService.security.entity.ChannelType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
public class MessageSchedulerDto {
    //Ahmed, This field used to specify the Target type if it is a User or Segment of users
    @NotNull
    private TargetType targetType;
    @NotNull(message = "Target ID must be provided")
    private Long targetId;

    @NotNull
    private ChannelType channel;

    private Long templateId;
    private String content;

    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledAt;
    @NotNull
    private Priority priority = Priority.LOW;
}

