package com.example.MessageService.segment.repository;

import com.example.MessageService.segment.entity.Segment;
import com.example.MessageService.template.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SegmentRepository extends JpaRepository<Segment,Long> {

    Optional<Segment> findByName(String name);
}
