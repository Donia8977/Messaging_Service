package com.example.MessageService.segment.service;

import com.example.MessageService.exception.NotFoundException;
import com.example.MessageService.exception.UnauthorizedException;
import com.example.MessageService.security.entity.Tenant;
import com.example.MessageService.security.entity.User;
import com.example.MessageService.security.repository.TenantRepository;
import com.example.MessageService.security.repository.UserRepository;
import com.example.MessageService.segment.dto.SegmentRequest;
import com.example.MessageService.segment.dto.SegmentResponse;
import com.example.MessageService.segment.entity.Segment;
import com.example.MessageService.segment.mapper.SegmentMapper;
import com.example.MessageService.segment.repository.SegmentRepository;
import com.example.MessageService.template.repository.TemplateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class SegmentServiceImpl implements SegmentService{
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private  final SegmentRepository segmentRepository;
    private  final SegmentMapper segmentMapper;

    public SegmentResponse createSegment(SegmentRequest request) {

        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new NotFoundException("Tenant not found"));


        Set<User> users = new HashSet<>(userRepository.findAllById(request.getUserIds()));

        boolean allUsersBelongToTenant = users.stream()
                .allMatch(user -> user.getTenant().getId().equals(tenant.getId()));

        if (!allUsersBelongToTenant) {
            throw new IllegalArgumentException("Some users do not belong to this tenant");
        }


        Segment segment = new Segment();
        segment.setName(request.getName());
        segment.setRulesJson(request.getRulesJson());
        segment.setUsers(users);
        segment.setTenant(tenant);

        Segment saved = segmentRepository.save(segment);

        return segmentMapper.mapToResponse(saved);
    }

    @Override
    public SegmentResponse updateSegment(Long id, SegmentRequest request) {
        Segment segment = segmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Segment not found"));


        segment.setName(request.getName());
        segment.setRulesJson(request.getRulesJson());


        Set<User> users = new HashSet<>(userRepository.findAllById(request.getUserIds()));
        segment.setUsers(users);


        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new NotFoundException("Tenant not found"));
        segment.setTenant(tenant);


        Segment updated = segmentRepository.save(segment);

        return segmentMapper.mapToResponse(updated);
    }

    @Override
    public List<SegmentResponse> getAllSegments(Long tenantId) {
        List<Segment> segments = segmentRepository.findByTenantId(tenantId);

        return segments.stream()
                .map(segmentMapper::mapToResponse)
                .collect(Collectors.toList());
    }
    @Override
    public SegmentResponse getSegmentById(Long Id , Long tenantId) {
        Segment segment = segmentRepository.findById(Id)
                .orElseThrow(() -> new NotFoundException("Segment not found"));

        if (!segment.getTenant().getId().equals(tenantId)) {
            throw new UnauthorizedException("You do not have access to this segment");
        }
        return segmentMapper.mapToResponse(segment);
    }

    @Override
    public void deleteSegment(Long Id , Long tenantId) {
        Segment segment = segmentRepository.findById(Id)
                .orElseThrow(() -> new NotFoundException("Segment not found"));
        if (!segment.getTenant().getId().equals(tenantId))
            throw new UnauthorizedException("You do not have access to this segment");
        segmentRepository.delete(segment);
    }
}

