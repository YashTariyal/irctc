package com.irctc.notification.repository;

import com.irctc.notification.entity.WhatsAppTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WhatsAppTemplateRepository extends JpaRepository<WhatsAppTemplate, Long> {
    Optional<WhatsAppTemplate> findByTemplateId(String templateId);
}

