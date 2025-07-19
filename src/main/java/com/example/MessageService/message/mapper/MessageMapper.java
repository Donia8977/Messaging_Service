package com.example.MessageService.message.mapper;

import com.example.MessageService.message.dto.MessageSchedulerDto;
import com.example.MessageService.message.entity.Message;
import com.example.MessageService.security.entity.User;
import com.example.MessageService.segment.entity.Segment;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class MessageMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "segment", ignore = true)
    public abstract Message toEntity(MessageSchedulerDto dto);

    public abstract List<MessageSchedulerDto> toDtoList(List<Message> entities);

    @Mapping(target = "targetId", expression = "java(resolveTargetId(entity))")
    public abstract MessageSchedulerDto toDto(Message entity);

    protected Long resolveTargetId(Message message) {
        if (message.getUser() != null) return message.getUser().getId();
        if (message.getSegment() != null) return message.getSegment().getId();
        return null;
    }

    @AfterMapping
    protected void afterToEntity(MessageSchedulerDto dto, @MappingTarget Message message) {
        if (dto.getTargetType() == null || dto.getTargetId() == null) return;

        switch (dto.getTargetType()) {
            case USER -> message.setUser(new User(dto.getTargetId()));
            case SEGMENT -> message.setSegment(new Segment(dto.getTargetId()));
        }
    }
}
