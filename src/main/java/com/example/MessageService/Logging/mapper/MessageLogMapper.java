package com.example.MessageService.Logging.mapper;

import com.example.MessageService.Logging.dto.MessageLogResponse;
import com.example.MessageService.Logging.entity.MessageLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageLogMapper {

//    @Mapping(source = "message.id", target = "messageId")
    MessageLogResponse toDto(MessageLog entity);


    List<MessageLogResponse> toDtoList(List<MessageLog> entities);
}