package com.example.MessageService.segment.controller;

import com.example.MessageService.segment.dto.SegmentRequest;
import com.example.MessageService.segment.dto.SegmentResponse;
import com.example.MessageService.segment.service.SegmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/segments")
@RequiredArgsConstructor
public class SegmentController {
    private final SegmentService segmentService;

    @PostMapping
    public ResponseEntity<SegmentResponse> createSegment(@RequestBody SegmentRequest request) {
        SegmentResponse response = segmentService.createSegment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all/{tenantId}")
    public ResponseEntity<List<SegmentResponse>> getAllSegments(@PathVariable Long tenantId)
    {
        List<SegmentResponse> segments = segmentService.getAllSegments(tenantId);
        return ResponseEntity.ok(segments);
    }

    @GetMapping("/{id}/{tenantId}")
    public ResponseEntity<SegmentResponse> getSegmentById(@PathVariable Long segmentId,@PathVariable Long tenantId)
    {
        SegmentResponse response = segmentService.getSegmentById(segmentId,tenantId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteSegment(@PathVariable Long segmentId,@PathVariable Long tenantId)
    {
        segmentService.deleteSegment(segmentId,tenantId);
        return ResponseEntity.noContent().build();
    }
}
