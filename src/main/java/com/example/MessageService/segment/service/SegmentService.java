package com.example.MessageService.segment.service;

import com.example.MessageService.segment.dto.SegmentRequest;
import com.example.MessageService.segment.dto.SegmentResponse;
import com.example.MessageService.segment.dto.SegmentRuleRequest;
import com.example.MessageService.template.dto.TemplateRequest;
import com.example.MessageService.template.dto.TemplateResponse;

import java.util.List;

public interface SegmentService {
    SegmentResponse createSegment (SegmentRequest request);
    SegmentResponse updateSegment (Long id , SegmentRequest request);
    List<SegmentResponse> getAllSegments(Long tenantId);
    SegmentResponse getSegmentById(Long Id, Long tenantId);
    void deleteSegment (Long Id, Long tenantId);
    SegmentResponse createSegmentFromRules (SegmentRuleRequest ruleRequest, Long tenantId);
    List<SegmentResponse> getAllSegmentsForCurrentTenant();
}
