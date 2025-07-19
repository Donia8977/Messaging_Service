package com.example.MessageService.segment.dto;

import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SegmentResponse {
    private Long id;
    private String name;
    private String rulesJson;
    private Set<Long> userIds = new HashSet<>();
    private Long tenantId;


}
