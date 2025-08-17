package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.Coach;
import com.irctc_backend.irctc.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    List<Seat> findByCoach(Coach coach);
    
    List<Seat> findByCoachAndStatus(Coach coach, Seat.SeatStatus status);
    
    Optional<Seat> findByCoachAndSeatNumber(Coach coach, String seatNumber);
    
    List<Seat> findByStatus(Seat.SeatStatus status);
    
    List<Seat> findBySeatType(Seat.SeatType seatType);
    
    List<Seat> findByBerthType(Seat.BerthType berthType);
    
    List<Seat> findByIsLadiesQuota(Boolean isLadiesQuota);
    
    List<Seat> findByIsSeniorCitizenQuota(Boolean isSeniorCitizenQuota);
    
    List<Seat> findByIsHandicappedFriendly(Boolean isHandicappedFriendly);
    
    @Query("SELECT s FROM Seat s WHERE s.coach = :coach AND s.status = 'AVAILABLE'")
    List<Seat> findAvailableSeatsByCoach(@Param("coach") Coach coach);
    
    @Query("SELECT s FROM Seat s WHERE s.coach = :coach AND s.seatType = :seatType AND s.status = 'AVAILABLE'")
    List<Seat> findAvailableSeatsByCoachAndType(@Param("coach") Coach coach, @Param("seatType") Seat.SeatType seatType);
    
    @Query("SELECT s FROM Seat s WHERE s.coach = :coach AND s.berthType = :berthType AND s.status = 'AVAILABLE'")
    List<Seat> findAvailableSeatsByCoachAndBerthType(@Param("coach") Coach coach, @Param("berthType") Seat.BerthType berthType);
    
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.coach = :coach AND s.status = 'AVAILABLE'")
    Long countAvailableSeatsByCoach(@Param("coach") Coach coach);
    
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.coach = :coach AND s.status = 'BOOKED'")
    Long countBookedSeatsByCoach(@Param("coach") Coach coach);
} 