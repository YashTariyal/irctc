package com.irctc.booking.entity;

import com.irctc.booking.tenant.TenantAware;
import com.irctc.booking.validation.ValidPnr;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
    name = "bookings",
    indexes = {
        @Index(name = "idx_bookings_pnr", columnList = "pnrNumber"),
        @Index(name = "idx_bookings_user", columnList = "userId"),
        @Index(name = "idx_bookings_train", columnList = "trainId"),
        @Index(name = "idx_bookings_status", columnList = "status"),
        @Index(name = "idx_bookings_tenant_id", columnList = "tenantId")
    }
)
@EntityListeners(com.irctc.booking.audit.EntityAuditListener.class)
@Data
public class SimpleBooking implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    @Column(nullable = false)
    private Long userId;
    
    @NotNull(message = "Train ID is required")
    @Positive(message = "Train ID must be positive")
    @Column(nullable = false)
    private Long trainId;
    
    @NotBlank(message = "PNR number is required")
    @ValidPnr
    @Column(nullable = false, unique = true)
    private String pnrNumber;
    
    @NotNull(message = "Booking time is required")
    @Column(nullable = false)
    private LocalDateTime bookingTime;
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "CONFIRMED|CANCELLED|PENDING|WAITLIST|RAC", 
             message = "Status must be one of: CONFIRMED, CANCELLED, PENDING, WAITLIST, RAC")
    @Column(nullable = false)
    private String status;
    
    @NotNull(message = "Total fare is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total fare must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Total fare must have at most 8 integer digits and 2 decimal places")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFare;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "booking_id")
    private List<SimplePassenger> passengers;
    
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
