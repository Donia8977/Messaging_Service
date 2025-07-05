package com.example.MessageService.template.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import com.example.MessageService.template.dto.TemplateResponse;
import com.example.MessageService.template.entity.Template;


import javax.xml.transform.Source;

@Mapper(componentModel = "spring")
public interface TemplateMapper {

    TemplateMapper INSTANCE = Mappers.getMapper(TemplateMapper.class);
    @Mapping(source = "id", target= "id")
    @Mapping(source = "name", target= "name")
    @Mapping(source = "content", target= "content")
    @Mapping(source = "createdAt", target= "createdAt")
    TemplateResponse mapToResponse(Template template);
}
