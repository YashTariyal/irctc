package com.irctc.booking.entity;

import com.irctc.booking.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity for hotel information
 */
@Entity
@Table(
    name = "hotels",
    indexes = {
        @Index(name = "idx_hotels_location", columnList = "location"),
        @Index(name = "idx_hotels_station_code", columnList = "nearestStationCode"),
        @Index(name = "idx_hotels_rating", columnList = "rating"),
        @Index(name = "idx_hotels_tenant_id", columnList = "tenantId")
    }
)
@Data
public class Hotel implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String location; // City or area name
    
    @Column(name = "nearest_station_code", length = 10)
    private String nearestStationCode; // Nearest railway station code
    
    @Column(length = 500)
    private String address;
    
    @Column(length = 20)
    private String city;
    
    @Column(length = 20)
    private String state;
    
    @Column(length = 10)
    private String pincode;
    
    @Column(length = 15)
    private String phone;
    
    @Column(length = 100)
    private String email;
    
    @Column(precision = 3, scale = 1)
    private BigDecimal rating; // 1.0 to 5.0
    
    @Column(name = "price_per_night", precision = 10, scale = 2)
    private BigDecimal pricePerNight;
    
    @Column(name = "total_rooms")
    private Integer totalRooms;
    
    @Column(name = "available_rooms")
    private Integer availableRooms;
    
    @Column(length = 500)
    private String amenities; // Comma-separated amenities
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "partner_hotel_id", length = 100)
    private String partnerHotelId; // ID from external hotel partner
    
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

