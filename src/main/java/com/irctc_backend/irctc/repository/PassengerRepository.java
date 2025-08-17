package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.Passenger;
import com.irctc_backend.irctc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    
    List<Passenger> findByUser(User user);
    
    List<Passenger> findByPassengerType(Passenger.PassengerType passengerType);
    
    List<Passenger> findByGender(Passenger.Gender gender);
    
    List<Passenger> findByIsSeniorCitizen(Boolean isSeniorCitizen);
    
    List<Passenger> findByIsLadiesQuota(Boolean isLadiesQuota);
    
    List<Passenger> findByIsHandicapped(Boolean isHandicapped);
    
    @Query("SELECT p FROM Passenger p WHERE p.user = :user AND p.firstName LIKE %:name% OR p.lastName LIKE %:name%")
    List<Passenger> findByUserAndNameContaining(@Param("user") User user, @Param("name") String name);
    
    @Query("SELECT p FROM Passenger p WHERE p.age >= :minAge AND p.age <= :maxAge")
    List<Passenger> findByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);
    
    Optional<Passenger> findByIdProofNumber(String idProofNumber);
} 