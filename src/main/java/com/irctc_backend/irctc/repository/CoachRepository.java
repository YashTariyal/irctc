package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.Coach;
import com.irctc_backend.irctc.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> {
    
    List<Coach> findByTrain(Train train);
    
    List<Coach> findByTrainAndIsActive(Train train, Boolean isActive);
    
    Optional<Coach> findByTrainAndCoachNumber(Train train, String coachNumber);
    
    List<Coach> findByCoachType(Coach.CoachType coachType);
    
    List<Coach> findByIsActive(Boolean isActive);
    
    @Query("SELECT c FROM Coach c WHERE c.train = :train AND c.availableSeats > 0 AND c.isActive = true")
    List<Coach> findAvailableCoachesByTrain(@Param("train") Train train);
    
    @Query("SELECT c FROM Coach c WHERE c.train = :train AND c.coachType = :coachType AND c.availableSeats > 0 AND c.isActive = true")
    List<Coach> findAvailableCoachesByTrainAndType(@Param("train") Train train, @Param("coachType") Coach.CoachType coachType);
    
    @Query("SELECT c FROM Coach c WHERE c.availableSeats > 0 AND c.isActive = true")
    List<Coach> findAllAvailableCoaches();
} 