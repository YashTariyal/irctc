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
import java.util.List;

@Entity
@Table(name = "passengers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Passenger {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-passengers")
    private User user;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(name = "age", nullable = false)
    private Integer age;
    
    @Enumerated(EnumType.STRING)
    private Gender gender;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_type")
    private PassengerType passengerType;
    
    @Column(name = "id_proof_type")
    @Enumerated(EnumType.STRING)
    private IdProofType idProofType;
    
    @Column(name = "id_proof_number")
    private String idProofNumber;
    
    @Column(name = "is_senior_citizen")
    private Boolean isSeniorCitizen = false;
    
    @Column(name = "is_ladies_quota")
    private Boolean isLadiesQuota = false;
    
    @Column(name = "is_handicapped")
    private Boolean isHandicapped = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "passenger", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("passenger-bookings")
    private List<Booking> bookings;
    
    public enum Gender {
        MALE, FEMALE, OTHER
    }
    
    public enum PassengerType {
        ADULT, CHILD, INFANT, SENIOR_CITIZEN
    }
    
    public enum IdProofType {
        AADHAR, PAN, PASSPORT, DRIVING_LICENSE, VOTER_ID, BIRTH_CERTIFICATE
    }
} 