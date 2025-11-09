package com.irctc.notification.tenant.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants", indexes = {
    @Index(name = "idx_tenants_code", columnList = "code", unique = true),
    @Index(name = "idx_tenants_status", columnList = "status")
})
@Data
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Tenant code is required")
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    
    @NotBlank(message = "Tenant name is required")
    @Column(nullable = false, length = 200)
    private String name;
    
    @Email(message = "Email must be valid")
    @Column(length = 255)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 50)
    private String status = "ACTIVE";
    
    @Column(columnDefinition = "TEXT")
    private String configuration;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "ACTIVE";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

