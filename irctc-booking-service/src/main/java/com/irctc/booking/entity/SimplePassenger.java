package com.irctc.booking.entity;

import com.irctc.booking.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "passengers")
@EntityListeners(com.irctc.booking.audit.EntityAuditListener.class)
@Data
public class SimplePassenger implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Integer age;
    
    @Column(nullable = false)
    private String gender;
    
    @Column(nullable = false)
    private String seatNumber;
    
    @Column(nullable = false)
    private String idProofType;
    
    @Column(nullable = false)
    private String idProofNumber;
    
    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
