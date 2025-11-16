package com.irctc.booking.entity;

import com.irctc.booking.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for hotel bookings
 */
@Entity
@Table(
    name = "hotel_bookings",
    indexes = {
        @Index(name = "idx_hotel_bookings_user_id", columnList = "userId"),
        @Index(name = "idx_hotel_bookings_hotel_id", columnList = "hotelId"),
        @Index(name = "idx_hotel_bookings_booking_id", columnList = "trainBookingId"),
        @Index(name = "idx_hotel_bookings_status", columnList = "status"),
        @Index(name = "idx_hotel_bookings_tenant_id", columnList = "tenantId")
    }
)
@Data
public class HotelBooking implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Long hotelId;
    
    @Column(name = "train_booking_id")
    private Long trainBookingId; // Associated train booking for package deals
    
    @Column(nullable = false, unique = true, length = 50)
    private String bookingReference; // Unique booking reference
    
    @Column(nullable = false)
    private LocalDate checkInDate;
    
    @Column(nullable = false)
    private LocalDate checkOutDate;
    
    @Column(nullable = false)
    private Integer numberOfRooms;
    
    @Column(nullable = false)
    private Integer numberOfGuests;
    
    @Column(name = "guest_name", nullable = false)
    private String guestName;
    
    @Column(name = "guest_email", length = 100)
    private String guestEmail;
    
    @Column(name = "guest_phone", length = 15)
    private String guestPhone;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount; // Discount for package deals
    
    @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalAmount;
    
    @Column(nullable = false)
    private String status; // PENDING, CONFIRMED, CANCELLED, CHECKED_IN, CHECKED_OUT
    
    @Column(name = "payment_status")
    private String paymentStatus; // PENDING, PAID, REFUNDED
    
    @Column(name = "is_package_deal")
    private Boolean isPackageDeal = false; // True if part of train + hotel package
    
    @Column(name = "cancellation_policy", length = 500)
    private String cancellationPolicy;
    
    @Column(name = "special_requests", length = 1000)
    private String specialRequests;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (bookingReference == null) {
            bookingReference = "HTL" + System.currentTimeMillis();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

