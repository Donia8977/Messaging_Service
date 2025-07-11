package com.example.MessageService.message.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDetails {
    private String from;
    private String to;
    private String content;
    private String password;
    private String subject;




}
