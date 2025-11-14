package com.irctc.notification.entity;

import com.irctc.notification.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity for WhatsApp message templates
 */
@Entity
@Table(name = "whatsapp_templates", indexes = {
    @Index(name = "idx_whatsapp_templates_tenant_id", columnList = "tenantId")
})
@Data
public class WhatsAppTemplate implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String templateId; // External template ID from WhatsApp
    
    @Column(nullable = false)
    private String name; // Template name
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // Template content with variables like {{name}}
    
    @Column(nullable = false)
    private String category; // UTILITY, MARKETING, AUTHENTICATION
    
    @Column(nullable = false)
    private String language; // en, hi, etc.
    
    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED
    
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

