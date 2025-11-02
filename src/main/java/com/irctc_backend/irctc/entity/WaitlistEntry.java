package com.irctc_backend.irctc.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for waitlist entries
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "waitlist_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-waitlist")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    @JsonBackReference("train-waitlist")
    private Train train;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    @JsonBackReference("coach-waitlist")
    private Coach coach;
    
    @Column(name = "journey_date", nullable = false)
    private LocalDateTime journeyDate;
    
    @Column(name = "waitlist_number", nullable = false)
    private Integer waitlistNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WaitlistStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "quota_type", nullable = false)
    private QuotaType quotaType;
    
    @Column(name = "passenger_count", nullable = false)
    private Integer passengerCount;
    
    @Column(name = "preferred_berth_type")
    private String preferredBerthType;
    
    @Column(name = "preferred_seat_type")
    private String preferredSeatType;
    
    @Column(name = "is_ladies_quota")
    private Boolean isLadiesQuota = false;
    
    @Column(name = "is_senior_citizen_quota")
    private Boolean isSeniorCitizenQuota = false;
    
    @Column(name = "is_handicapped_friendly")
    private Boolean isHandicappedFriendly = false;
    
    @Column(name = "priority_score")
    private Integer priorityScore = 0;
    
    @Column(name = "auto_upgrade_enabled")
    private Boolean autoUpgradeEnabled = true;
    
    @Column(name = "notification_sent")
    private Boolean notificationSent = false;
    
    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "cancellation_reason")
    private String cancellationReason;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum WaitlistStatus {
        PENDING,        // Waiting for confirmation
        CONFIRMED,      // Confirmed from waitlist
        RAC,           // Reservation Against Cancellation
        CANCELLED,     // Cancelled by user
        EXPIRED,       // Expired due to time limit
        AUTO_CANCELLED // Auto-cancelled by system
    }
    
    public enum QuotaType {
        GENERAL, TATKAL, LADIES, SENIOR_CITIZEN, HANDICAPPED, 
        DEFENCE, PARLIAMENT, FOREIGN_TOURIST, PREMIUM_TATKAL
    }
}
