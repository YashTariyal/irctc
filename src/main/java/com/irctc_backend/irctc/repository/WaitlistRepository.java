package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.WaitlistEntry;
import com.irctc_backend.irctc.entity.Train;
import com.irctc_backend.irctc.entity.Coach;
import com.irctc_backend.irctc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for WaitlistEntry entity
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {
    
    List<WaitlistEntry> findByUser(User user);
    
    List<WaitlistEntry> findByTrain(Train train);
    
    List<WaitlistEntry> findByCoach(Coach coach);
    
    List<WaitlistEntry> findByJourneyDate(LocalDateTime journeyDate);
    
    List<WaitlistEntry> findByStatus(WaitlistEntry.WaitlistStatus status);
    
    List<WaitlistEntry> findByQuotaType(WaitlistEntry.QuotaType quotaType);
    
    @Query("SELECT w FROM WaitlistEntry w WHERE w.train = :train AND w.journeyDate = :journeyDate AND w.status = 'PENDING' ORDER BY w.waitlistNumber ASC")
    List<WaitlistEntry> findPendingWaitlistByTrainAndDate(@Param("train") Train train, @Param("journeyDate") LocalDateTime journeyDate);
    
    @Query("SELECT w FROM WaitlistEntry w WHERE w.coach = :coach AND w.journeyDate = :journeyDate AND w.status = 'PENDING' ORDER BY w.waitlistNumber ASC")
    List<WaitlistEntry> findPendingWaitlistByCoachAndDate(@Param("coach") Coach coach, @Param("journeyDate") LocalDateTime journeyDate);
    
    @Query("SELECT w FROM WaitlistEntry w WHERE w.train = :train AND w.journeyDate = :journeyDate AND w.quotaType = :quotaType AND w.status = 'PENDING' ORDER BY w.waitlistNumber ASC")
    List<WaitlistEntry> findPendingWaitlistByTrainDateAndQuota(@Param("train") Train train, @Param("journeyDate") LocalDateTime journeyDate, @Param("quotaType") WaitlistEntry.QuotaType quotaType);
    
    @Query("SELECT MAX(w.waitlistNumber) FROM WaitlistEntry w WHERE w.train = :train AND w.journeyDate = :journeyDate AND w.quotaType = :quotaType")
    Optional<Integer> findMaxWaitlistNumberByTrainDateAndQuota(@Param("train") Train train, @Param("journeyDate") LocalDateTime journeyDate, @Param("quotaType") WaitlistEntry.QuotaType quotaType);
    
    @Query("SELECT COUNT(w) FROM WaitlistEntry w WHERE w.train = :train AND w.journeyDate = :journeyDate AND w.status = 'PENDING'")
    Long countPendingWaitlistByTrainAndDate(@Param("train") Train train, @Param("journeyDate") LocalDateTime journeyDate);
    
    @Query("SELECT COUNT(w) FROM WaitlistEntry w WHERE w.coach = :coach AND w.journeyDate = :journeyDate AND w.status = 'PENDING'")
    Long countPendingWaitlistByCoachAndDate(@Param("coach") Coach coach, @Param("journeyDate") LocalDateTime journeyDate);
    
    @Query("SELECT w FROM WaitlistEntry w WHERE w.user = :user AND w.journeyDate >= :date AND w.status = 'PENDING'")
    List<WaitlistEntry> findActiveWaitlistByUser(@Param("user") User user, @Param("date") LocalDateTime date);
    
    @Query("SELECT w FROM WaitlistEntry w WHERE w.expiryTime <= :currentTime AND w.status = 'PENDING'")
    List<WaitlistEntry> findExpiredWaitlistEntries(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT w FROM WaitlistEntry w WHERE w.train = :train AND w.journeyDate = :journeyDate AND w.quotaType = :quotaType AND w.status = 'PENDING' AND w.waitlistNumber <= :maxWaitlistNumber ORDER BY w.waitlistNumber ASC")
    List<WaitlistEntry> findEligibleWaitlistEntries(@Param("train") Train train, @Param("journeyDate") LocalDateTime journeyDate, @Param("quotaType") WaitlistEntry.QuotaType quotaType, @Param("maxWaitlistNumber") Integer maxWaitlistNumber);
    
    @Query("SELECT w FROM WaitlistEntry w WHERE w.autoUpgradeEnabled = true AND w.status = 'PENDING' AND w.journeyDate >= :currentDate")
    List<WaitlistEntry> findAutoUpgradeEligibleEntries(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT w FROM WaitlistEntry w WHERE w.notificationSent = false AND w.status IN ('CONFIRMED', 'RAC')")
    List<WaitlistEntry> findUnnotifiedConfirmedEntries();
    
    @Query("SELECT w FROM WaitlistEntry w WHERE w.train = :train AND w.journeyDate = :journeyDate AND w.quotaType = :quotaType AND w.status = 'PENDING' AND w.waitlistNumber = :waitlistNumber")
    Optional<WaitlistEntry> findByTrainDateQuotaAndWaitlistNumber(@Param("train") Train train, @Param("journeyDate") LocalDateTime journeyDate, @Param("quotaType") WaitlistEntry.QuotaType quotaType, @Param("waitlistNumber") Integer waitlistNumber);
}
