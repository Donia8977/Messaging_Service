package com.example.MessageService.message.mapper;
import com.example.MessageService.message.dto.MessageSchedulerDto;
import com.example.MessageService.message.entity.Message;
import org.mapstruct.Mapper;
import java.util.List;


@Mapper(componentModel = "spring")
public interface MessageMapper {
    MessageSchedulerDto toDto(Message entity);
    List<MessageSchedulerDto> toDtoList(List<Message> entities);
    Message toEntity(MessageSchedulerDto dto);
}
