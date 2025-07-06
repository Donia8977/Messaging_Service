package com.example.MessageService.template.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.MessageService.template.entity.Template;

import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template,Long> {

    Optional<Template> findByName(String name);
}
