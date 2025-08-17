package com.irctc_backend.irctc.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "seats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    @JsonBackReference("coach-seats")
    private Coach coach;
    
    @Column(name = "seat_number", nullable = false)
    private String seatNumber;
    
    @Column(name = "berth_number")
    private String berthNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "berth_type")
    private BerthType berthType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_status", nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;
    
    @Column(name = "is_ladies_quota")
    private Boolean isLadiesQuota = false;
    
    @Column(name = "is_senior_citizen_quota")
    private Boolean isSeniorCitizenQuota = false;
    
    @Column(name = "is_handicapped_friendly")
    private Boolean isHandicappedFriendly = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum SeatType {
        WINDOW, AISLE, MIDDLE, SIDE_UPPER, SIDE_LOWER
    }
    
    public enum BerthType {
        LOWER, MIDDLE, UPPER, SIDE_LOWER, SIDE_UPPER
    }
    
    public enum SeatStatus {
        AVAILABLE, BOOKED, RESERVED, MAINTENANCE, BLOCKED
    }
} 