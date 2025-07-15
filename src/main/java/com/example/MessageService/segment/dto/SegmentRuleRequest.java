package com.example.MessageService.segment.dto;

import com.example.MessageService.security.entity.UserType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;


@Data
public class SegmentRuleRequest {
    @NotBlank(message = "Segment name cannot be blank")
    private String name;
    private List<String> cities;
    private List<UserType> userTypes;
    private int age;
    //private List<String> genders;
}