//package com.example.MessageService.segment.service;
//
//import com.example.MessageService.security.entity.Tenant;
//import com.example.MessageService.security.entity.User;
//import com.example.MessageService.security.repository.TenantRepository;
//import com.example.MessageService.segment.dto.SegmentRequest;
//import com.example.MessageService.segment.dto.SegmentResponse;
//import com.example.MessageService.segment.entity.Segment;
//import com.example.MessageService.segment.repository.SegmentRepository;
//import com.example.MessageService.template.entity.Template;
//import com.example.MessageService.template.repository.TemplateRepository;
//
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//public class SegmentServiceImpl implements SegmentService{
//    private final SegmentRepository segmentRepository;
//    private final TenantRepository tenantRepository;
//
//    public SegmentServiceImpl(SegmentRepository segmentRepository , TenantRepository tenantRepository){
//        this.segmentRepository = segmentRepository;
//        this.tenantRepository = tenantRepository;
//    }
//
//    @Override
//    public SegmentResponse createSegment(SegmentRequest request) {
//        Segment segment = new Segment();
//        segment.setName(request.getName());
//        segment.setRulesJson(request.getRulesJson());
//
//        // جلب المستخدمين من قاعدة البيانات
//        Set<User> users = new HashSet<>(userRepository.findAllById(request.getUserIds()));
//        segment.setUsers(users);
//
//        // جلب التينانت من قاعدة البيانات
//        Tenant tenant = tenantRepository.findById(request.getTenantId())
//                .orElseThrow(() -> new RuntimeException("Tenant not found"));
//        segment.setTenant(tenant);
//
//        // حفظ الـ Segment
//        Segment saved = segmentRepository.save(segment);
//
//        // إرجاع response
//        return segmentMapper.mapToResponse(saved);
//    }
//
//    @Override
//    public SegmentResponse updateSegment(Long id, SegmentRequest request) {
//        return null;
//    }
//
//    @Override
//    public List<SegmentResponse> getAllSegments() {
//        return List.of();
//    }
//
//    @Override
//    public SegmentResponse getSegmentById(Long Id) {
//        return null;
//    }
//
//    @Override
//    public void deleteSegment(Long Id) {
//
//    }
//}
