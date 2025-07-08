package com.example.MessageService.template.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import com.example.MessageService.template.dto.TemplateResponse;
import com.example.MessageService.template.entity.Template;


import javax.xml.transform.Source;

@Mapper(componentModel = "spring")
public interface TemplateMapper {


    TemplateResponse mapToResponse(Template template);
}
