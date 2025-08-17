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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "pnr_number", unique = true, nullable = false)
    private String pnrNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-bookings")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    @JsonBackReference("train-bookings")
    private Train train;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    @JsonBackReference("passenger-bookings")
    private Passenger passenger;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    @JsonBackReference("seat-bookings")
    private Seat seat;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    @JsonBackReference("coach-bookings")
    private Coach coach;
    
    @Column(name = "journey_date", nullable = false)
    private LocalDate journeyDate;
    
    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;
    
    @Column(name = "total_fare", nullable = false)
    private BigDecimal totalFare;
    
    @Column(name = "base_fare")
    private BigDecimal baseFare;
    
    @Column(name = "tatkal_fare")
    private BigDecimal tatkalFare;
    
    @Column(name = "convenience_fee")
    private BigDecimal convenienceFee;
    
    @Column(name = "gst_amount")
    private BigDecimal gstAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "quota_type")
    private QuotaType quotaType = QuotaType.GENERAL;
    
    @Column(name = "is_tatkal")
    private Boolean isTatkal = false;
    
    @Column(name = "is_cancelled")
    private Boolean isCancelled = false;
    
    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;
    
    @Column(name = "refund_amount")
    private BigDecimal refundAmount;
    
    @Column(name = "booking_source")
    private String bookingSource; // WEB, MOBILE_APP, COUNTER, etc.
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("booking-payments")
    private List<Payment> payments;
    
    public enum BookingStatus {
        CONFIRMED, WAITLIST, RAC, CANCELLED, COMPLETED
    }
    
    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED, PARTIALLY_REFUNDED
    }
    
    public enum QuotaType {
        GENERAL, LADIES, SENIOR_CITIZEN, HANDICAPPED, TATKAL, PREMIUM_TATKAL
    }
} 