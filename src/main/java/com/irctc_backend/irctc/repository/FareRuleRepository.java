package com.irctc_backend.irctc.repository;

import com.irctc_backend.irctc.entity.Coach;
import com.irctc_backend.irctc.entity.FareRule;
import com.irctc_backend.irctc.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FareRule entity
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface FareRuleRepository extends JpaRepository<FareRule, Long> {
    
    /**
     * Find active fare rule for a specific train and coach type
     */
    @Query("SELECT fr FROM FareRule fr WHERE fr.train = :train AND fr.coachType = :coachType " +
           "AND fr.isActive = true AND (fr.validFrom IS NULL OR fr.validFrom <= :date) " +
           "AND (fr.validUntil IS NULL OR fr.validUntil >= :date)")
    Optional<FareRule> findActiveFareRule(@Param("train") Train train, 
                                         @Param("coachType") Coach.CoachType coachType, 
                                         @Param("date") LocalDateTime date);
    
    /**
     * Find all active fare rules for a train
     */
    @Query("SELECT fr FROM FareRule fr WHERE fr.train = :train AND fr.isActive = true " +
           "AND (fr.validFrom IS NULL OR fr.validFrom <= :date) " +
           "AND (fr.validUntil IS NULL OR fr.validUntil >= :date)")
    List<FareRule> findActiveFareRulesForTrain(@Param("train") Train train, 
                                              @Param("date") LocalDateTime date);
    
    /**
     * Find fare rules by train and coach type
     */
    List<FareRule> findByTrainAndCoachType(Train train, Coach.CoachType coachType);
    
    /**
     * Find active fare rules by train and coach type
     */
    List<FareRule> findByTrainAndCoachTypeAndIsActiveTrue(Train train, Coach.CoachType coachType);
    
    /**
     * Find fare rules within a distance range
     */
    @Query("SELECT fr FROM FareRule fr WHERE fr.train = :train AND fr.coachType = :coachType " +
           "AND fr.distanceKm BETWEEN :minDistance AND :maxDistance AND fr.isActive = true")
    List<FareRule> findFareRulesByDistanceRange(@Param("train") Train train, 
                                               @Param("coachType") Coach.CoachType coachType,
                                               @Param("minDistance") Integer minDistance, 
                                               @Param("maxDistance") Integer maxDistance);
    
    /**
     * Find the closest fare rule for a given distance
     */
    @Query("SELECT fr FROM FareRule fr WHERE fr.train = :train AND fr.coachType = :coachType " +
           "AND fr.isActive = true ORDER BY ABS(fr.distanceKm - :distance) ASC")
    List<FareRule> findClosestFareRuleByDistance(@Param("train") Train train, 
                                                @Param("coachType") Coach.CoachType coachType,
                                                @Param("distance") Integer distance);
}
