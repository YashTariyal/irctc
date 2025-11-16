package com.irctc.booking.entity;

import com.irctc.booking.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity for tracking check-in status of bookings
 */
@Entity
@Table(
    name = "check_ins",
    indexes = {
        @Index(name = "idx_check_ins_booking_id", columnList = "bookingId"),
        @Index(name = "idx_check_ins_user_id", columnList = "userId"),
        @Index(name = "idx_check_ins_status", columnList = "status"),
        @Index(name = "idx_check_ins_tenant_id", columnList = "tenantId")
    }
)
@Data
public class CheckIn implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long bookingId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String pnrNumber;
    
    @Column(nullable = false)
    private String status; // PENDING, CHECKED_IN, FAILED, CANCELLED
    
    private String seatNumber; // Assigned seat number during check-in
    
    private String coachNumber; // Assigned coach number
    
    private LocalDateTime checkInTime; // When check-in was completed
    
    private LocalDateTime scheduledCheckInTime; // When auto check-in is scheduled
    
    private LocalDateTime departureTime; // Train departure time
    
    private String checkInMethod; // AUTO, MANUAL
    
    private String failureReason; // Reason if check-in failed
    
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

