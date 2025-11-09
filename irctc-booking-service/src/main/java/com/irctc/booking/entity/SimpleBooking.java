package com.irctc.booking.entity;

import jakarta.persistence.*;
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
        @Index(name = "idx_bookings_status", columnList = "status")
    }
)
@Data
public class SimpleBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Long trainId;
    
    @Column(nullable = false)
    private String pnrNumber;
    
    @Column(nullable = false)
    private LocalDateTime bookingTime;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFare;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "booking_id")
    private List<SimplePassenger> passengers;
    
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
