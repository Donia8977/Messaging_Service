package com.example.MessageService.template.service;

import com.example.MessageService.template.dto.TemplateRequest;
import com.example.MessageService.template.dto.TemplateResponse;

import java.util.List;
import java.util.Map;

public interface TemplateService {
    TemplateResponse createTemplate (TemplateRequest request);
    TemplateResponse updateTemplate (Long id , TemplateRequest request);
    List<TemplateResponse> getAllTemplates();
    TemplateResponse getTemplateById(Long Id);
    void deleteTemplate (Long Id);
    String renderTemplate(String templateContent, Map<String, String> data);
}
