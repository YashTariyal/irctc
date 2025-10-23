package com.irctc.train.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trains")
@Data
public class SimpleTrain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String trainNumber;
    
    @Column(nullable = false)
    private String trainName;
    
    @Column(nullable = false)
    private String sourceStation;
    
    @Column(nullable = false)
    private String destinationStation;
    
    @Column(nullable = false)
    private LocalDateTime departureTime;
    
    @Column(nullable = false)
    private LocalDateTime arrivalTime;
    
    @Column(nullable = false)
    private String trainType;
    
    @Column(nullable = false)
    private String trainClass;
    
    @Column(nullable = false)
    private Double baseFare;
    
    @Column(nullable = false)
    private Integer totalSeats;
    
    @Column(nullable = false)
    private Integer availableSeats;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @ElementCollection
    @CollectionTable(name = "train_amenities", joinColumns = @JoinColumn(name = "train_id"))
    @Column(name = "amenity")
    private List<String> amenities;
    
    @Column(columnDefinition = "TEXT")
    private String routeDescription;
    
    @Column(nullable = false)
    private Integer distance;
    
    @Column(nullable = false)
    private Integer duration;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
