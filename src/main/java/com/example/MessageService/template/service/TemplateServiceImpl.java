package com.example.MessageService.template.service;

import com.example.MessageService.exception.NotFoundException;
import com.example.MessageService.security.entity.User;
import com.example.MessageService.template.dto.TemplateRequest;
import com.example.MessageService.template.dto.TemplateResponse;
import com.example.MessageService.template.entity.Template;
import com.example.MessageService.template.mapper.TemplateMapper;
import com.example.MessageService.template.repository.TemplateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor

@Service
public class TemplateServiceImpl implements TemplateService{

    private final TemplateRepository templateRepository;
    private final TemplateMapper templateMapper;



    @Override
    public TemplateResponse createTemplate(TemplateRequest request) {
        Template template = new Template();
        template.setName(request.getName());
        template.setContent(request.getContent());
        template.setCreatedAt(LocalDateTime.now());
        Template saved = templateRepository.save(template);
        return templateMapper.mapToResponse(saved);
    }

    @Override
    public TemplateResponse updateTemplate(Long id, TemplateRequest request) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Template not found"));
        template.setName(request.getName());
        template.setContent(request.getContent());
        template.setUpdatedAt(LocalDateTime.now());

        Template updated = templateRepository.save(template);
        return  templateMapper.mapToResponse(updated);
    }

    @Override
    public List<TemplateResponse> getAllTemplates() {
        return templateRepository.findAll()
                .stream()
                .map(templateMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TemplateResponse getTemplateById(Long Id) {
        Template template = templateRepository.findById(Id)
                .orElseThrow(() -> new NotFoundException("template not found") );
        return templateMapper.mapToResponse(template);

    }

    @Override
    public void deleteTemplate(Long Id) {
        Template template = templateRepository.findById(Id)
                .orElseThrow(() -> new NotFoundException("template not found"));
        templateRepository.deleteById(Id);
    }




    @Override
    public String renderTemplate(String templateContent, Map<String, String> data) {
        String rendered = templateContent;

        for (Map.Entry<String, String> entry : data.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            rendered = rendered.replace(placeholder, entry.getValue());
        }

        return rendered;
    }

}
