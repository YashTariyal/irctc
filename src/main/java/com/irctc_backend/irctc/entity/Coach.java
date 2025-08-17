package com.irctc_backend.irctc.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "coaches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coach {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    @JsonBackReference("train-coaches")
    private Train train;
    
    @Column(name = "coach_number", nullable = false)
    private String coachNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "coach_type", nullable = false)
    private CoachType coachType;
    
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;
    
    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;
    
    @Column(name = "base_fare", nullable = false)
    private BigDecimal baseFare;
    
    @Column(name = "ac_fare")
    private BigDecimal acFare;
    
    @Column(name = "sleeper_fare")
    private BigDecimal sleeperFare;
    
    @Column(name = "tatkal_fare")
    private BigDecimal tatkalFare;
    
    @Column(name = "ladies_quota")
    private Integer ladiesQuota;
    
    @Column(name = "senior_citizen_quota")
    private Integer seniorCitizenQuota;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("coach-seats")
    private List<Seat> seats;
    
    public enum CoachType {
        AC_FIRST_CLASS, AC_2_TIER, AC_3_TIER, SLEEPER_CLASS, SECOND_SITTING, 
        AC_CHAIR_CAR, EXECUTIVE_CHAIR_CAR, FIRST_CLASS, LUGGAGE_VAN, BRAKE_VAN
    }
} 