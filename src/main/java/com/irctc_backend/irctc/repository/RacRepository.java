package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.RacEntry;
import com.irctc_backend.irctc.entity.Train;
import com.irctc_backend.irctc.entity.Coach;
import com.irctc_backend.irctc.entity.User;
import com.irctc_backend.irctc.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RacEntry entity
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface RacRepository extends JpaRepository<RacEntry, Long> {
    
    List<RacEntry> findByUser(User user);
    
    List<RacEntry> findByTrain(Train train);
    
    List<RacEntry> findByCoach(Coach coach);
    
    List<RacEntry> findBySeat(Seat seat);
    
    List<RacEntry> findByJourneyDate(LocalDateTime journeyDate);
    
    List<RacEntry> findByStatus(RacEntry.RacStatus status);
    
    List<RacEntry> findByQuotaType(RacEntry.QuotaType quotaType);
    
    @Query("SELECT r FROM RacEntry r WHERE r.train = :train AND r.journeyDate = :journeyDate AND r.status = 'RAC' ORDER BY r.racNumber ASC")
    List<RacEntry> findActiveRacByTrainAndDate(@Param("train") Train train, @Param("journeyDate") LocalDateTime journeyDate);
    
    @Query("SELECT r FROM RacEntry r WHERE r.coach = :coach AND r.journeyDate = :journeyDate AND r.status = 'RAC' ORDER BY r.racNumber ASC")
    List<RacEntry> findActiveRacByCoachAndDate(@Param("coach") Coach coach, @Param("journeyDate") LocalDateTime journeyDate);
    
    @Query("SELECT r FROM RacEntry r WHERE r.train = :train AND r.journeyDate = :journeyDate AND r.quotaType = :quotaType AND r.status = 'RAC' ORDER BY r.racNumber ASC")
    List<RacEntry> findActiveRacByTrainDateAndQuota(@Param("train") Train train, @Param("journeyDate") LocalDateTime journeyDate, @Param("quotaType") RacEntry.QuotaType quotaType);
    
    @Query("SELECT MAX(r.racNumber) FROM RacEntry r WHERE r.train = :train AND r.journeyDate = :journeyDate AND r.quotaType = :quotaType")
    Optional<Integer> findMaxRacNumberByTrainDateAndQuota(@Param("train") Train train, @Param("journeyDate") LocalDateTime journeyDate, @Param("quotaType") RacEntry.QuotaType quotaType);
    
    @Query("SELECT COUNT(r) FROM RacEntry r WHERE r.train = :train AND r.journeyDate = :journeyDate AND r.status = 'RAC'")
    Long countActiveRacByTrainAndDate(@Param("train") Train train, @Param("journeyDate") LocalDateTime journeyDate);
    
    @Query("SELECT COUNT(r) FROM RacEntry r WHERE r.train = :train AND r.journeyDate = :journeyDate")
    Long countByTrainAndJourneyDate(@Param("train") Train train, @Param("journeyDate") LocalDateTime journeyDate);
    
    @Query("SELECT COUNT(r) FROM RacEntry r WHERE r.train = :train AND r.journeyDate = :journeyDate AND r.status = :status")
    Long countByTrainJourneyDateAndStatus(@Param("train") Train train, @Param("journeyDate") LocalDateTime journeyDate, @Param("status") RacEntry.RacStatus status);
    
    @Query("SELECT COUNT(r) FROM RacEntry r WHERE r.coach = :coach AND r.journeyDate = :journeyDate AND r.status = 'RAC'")
    Long countActiveRacByCoachAndDate(@Param("coach") Coach coach, @Param("journeyDate") LocalDateTime journeyDate);
    
    @Query("SELECT r FROM RacEntry r WHERE r.user = :user AND r.journeyDate >= :date AND r.status = 'RAC'")
    List<RacEntry> findActiveRacByUser(@Param("user") User user, @Param("date") LocalDateTime date);
    
    @Query("SELECT r FROM RacEntry r WHERE r.train = :train AND r.journeyDate = :journeyDate AND r.quotaType = :quotaType AND r.status = 'RAC' AND r.racNumber <= :maxRacNumber ORDER BY r.racNumber ASC")
    List<RacEntry> findEligibleRacEntries(@Param("train") Train train, @Param("journeyDate") LocalDateTime journeyDate, @Param("quotaType") RacEntry.QuotaType quotaType, @Param("maxRacNumber") Integer maxRacNumber);
    
    @Query("SELECT r FROM RacEntry r WHERE r.autoUpgradeEnabled = true AND r.status = 'RAC' AND r.journeyDate >= :currentDate")
    List<RacEntry> findAutoUpgradeEligibleRacEntries(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT r FROM RacEntry r WHERE r.notificationSent = false AND r.status = 'CONFIRMED'")
    List<RacEntry> findUnnotifiedConfirmedRacEntries();
    
    @Query("SELECT r FROM RacEntry r WHERE r.train = :train AND r.journeyDate = :journeyDate AND r.quotaType = :quotaType AND r.status = 'RAC' AND r.racNumber = :racNumber")
    Optional<RacEntry> findByTrainDateQuotaAndRacNumber(@Param("train") Train train, @Param("journeyDate") LocalDateTime journeyDate, @Param("quotaType") RacEntry.QuotaType quotaType, @Param("racNumber") Integer racNumber);
    
    @Query("SELECT r FROM RacEntry r WHERE r.seat = :seat AND r.journeyDate = :journeyDate AND r.status = 'RAC'")
    List<RacEntry> findRacBySeatAndDate(@Param("seat") Seat seat, @Param("journeyDate") LocalDateTime journeyDate);
    
    @Query("SELECT r FROM RacEntry r WHERE r.coach = :coach AND r.journeyDate = :journeyDate AND r.status = 'RAC' ORDER BY r.racNumber ASC LIMIT 1")
    Optional<RacEntry> findFirstRacByCoachAndDate(@Param("coach") Coach coach, @Param("journeyDate") LocalDateTime journeyDate);
}
