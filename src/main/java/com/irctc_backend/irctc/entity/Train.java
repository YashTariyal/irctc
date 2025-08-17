package com.irctc_backend.irctc.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "trains")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Train {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "train_number", unique = true, nullable = false)
    private String trainNumber;
    
    @Column(name = "train_name", nullable = false)
    private String trainName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_station_id", nullable = false)
    @JsonBackReference("station-source-trains")
    private Station sourceStation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_station_id", nullable = false)
    @JsonBackReference("station-destination-trains")
    private Station destinationStation;
    
    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;
    
    @Column(name = "arrival_time", nullable = false)
    private LocalTime arrivalTime;
    
    @Column(name = "journey_duration")
    private Integer journeyDuration; // in minutes
    
    @Column(name = "total_distance")
    private Double totalDistance; // in kilometers
    
    @Enumerated(EnumType.STRING)
    private TrainType trainType;
    
    @Enumerated(EnumType.STRING)
    private TrainStatus status = TrainStatus.ACTIVE;
    
    @Column(name = "is_running")
    private Boolean isRunning = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("train-schedules")
    private List<TrainSchedule> schedules;
    
    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("train-coaches")
    private List<Coach> coaches;
    
    public enum TrainType {
        EXPRESS, PASSENGER, SUPERFAST, RAJDHANI, SHATABDI, DURONTO, GARIB_RATH
    }
    
    public enum TrainStatus {
        ACTIVE, INACTIVE, MAINTENANCE, CANCELLED
    }
} 