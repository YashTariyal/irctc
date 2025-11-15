package com.irctc.booking.entity;

import com.irctc.booking.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * QR Code Entity
 * Stores QR code data for ticket verification
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "qr_codes", indexes = {
    @Index(name = "idx_qr_codes_booking_id", columnList = "bookingId"),
    @Index(name = "idx_qr_codes_code", columnList = "qrCode"),
    @Index(name = "idx_qr_codes_tenant_id", columnList = "tenantId")
})
@org.hibernate.annotations.FilterDef(
    name = "tenantFilter",
    parameters = @org.hibernate.annotations.ParamDef(name = "tenantId", type = String.class)
)
@org.hibernate.annotations.Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
@EntityListeners(com.irctc.booking.audit.EntityAuditListener.class)
@Data
public class QRCode implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long bookingId;
    
    @Column(nullable = false, unique = true, length = 500)
    private String qrCode; // Encrypted QR code data
    
    @Column(nullable = false, length = 100)
    private String pnrNumber;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private Boolean isVerified = false;
    
    @Column
    private LocalDateTime verifiedAt;
    
    @Column(length = 100)
    private String verifiedBy; // User ID or device ID who verified
    
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
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

