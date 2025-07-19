package com.example.MessageService.segment.mapper;

import com.example.MessageService.security.entity.User;
import com.example.MessageService.segment.dto.SegmentResponse;
import com.example.MessageService.segment.entity.Segment;
import com.example.MessageService.template.dto.TemplateResponse;
import com.example.MessageService.template.entity.Template;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SegmentMapper {

    @Mapping(target = "tenantId", source="tenant.id")
    @Mapping(target = "userIds", expression = "java(mapUserIds(segment))")
    SegmentResponse mapToResponse(Segment segment);

    default Set<Long> mapUserIds(Segment segment) {
        return segment.getUsers()
                .stream()
                .map(User::getId)
                .collect(Collectors.toSet());
    }
}

