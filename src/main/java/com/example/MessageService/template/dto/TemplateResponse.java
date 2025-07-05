package com.example.MessageService.template.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TemplateResponse {
    private Long id;
    private String name;
    private String content;
    private LocalDateTime createdAt;

}
