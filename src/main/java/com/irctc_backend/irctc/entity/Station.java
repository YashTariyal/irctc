package com.irctc_backend.irctc.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "stations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Station {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "station_code", unique = true, nullable = false)
    private String stationCode;
    
    @Column(name = "station_name", nullable = false)
    private String stationName;
    
    @Column(name = "station_type")
    @Enumerated(EnumType.STRING)
    private StationType stationType;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "state")
    private String state;
    
    @Column(name = "zone")
    private String zone;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "platform_count")
    private Integer platformCount;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "sourceStation", fetch = FetchType.LAZY)
    @JsonManagedReference("station-source-trains")
    private List<Train> sourceTrains;
    
    @OneToMany(mappedBy = "destinationStation", fetch = FetchType.LAZY)
    @JsonManagedReference("station-destination-trains")
    private List<Train> destinationTrains;
    
    public enum StationType {
        JUNCTION, TERMINAL, HALT, STATION
    }
} 