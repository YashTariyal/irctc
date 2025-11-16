package com.irctc.booking.repository;

import com.irctc.booking.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    Optional<CheckIn> findByBookingId(Long bookingId);
    
    List<CheckIn> findByUserIdAndStatus(Long userId, String status);
    
    List<CheckIn> findByStatusAndScheduledCheckInTimeBefore(String status, LocalDateTime time);
    
    @Query("SELECT c FROM CheckIn c WHERE c.bookingId = :bookingId AND c.status = 'CHECKED_IN'")
    Optional<CheckIn> findCheckedInByBookingId(Long bookingId);
    
    @Query("SELECT c FROM CheckIn c WHERE c.userId = :userId AND c.status = 'PENDING' AND c.departureTime > :now")
    List<CheckIn> findPendingCheckInsForUser(Long userId, LocalDateTime now);
}

