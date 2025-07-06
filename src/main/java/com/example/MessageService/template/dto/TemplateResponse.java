package com.example.MessageService.template.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateResponse {
    private Long id;
    private String name;
    private String content;
    private LocalDateTime createdAt;

}
