package com.example.MessageService.segment.repository;

import com.example.MessageService.segment.entity.Segment;
import com.example.MessageService.template.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface SegmentRepository extends JpaRepository<Segment,Long> {

    List<Segment> findByTenantId(Long tenantId);
    Optional<Segment> findByName(String name);

    @Query("SELECT s FROM Segment s JOIN s.users u WHERE u.id = :userId")
    List<Segment> findSegmentsByUserId(@Param("userId") Long userId);
}
