package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.dto.WaitlistRequest;
import com.irctc_backend.irctc.dto.WaitlistResponse;
import com.irctc_backend.irctc.entity.*;
import com.irctc_backend.irctc.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for waitlist and RAC management
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
@Transactional
public class WaitlistRacService {
    
    private static final Logger logger = LoggerFactory.getLogger(WaitlistRacService.class);
    
    @Autowired
    private WaitlistRepository waitlistRepository;
    
    @Autowired
    private RacRepository racRepository;
    
    @Autowired
    private TrainRepository trainRepository;
    
    @Autowired
    private CoachRepository coachRepository;
    
    @Autowired
    private SeatRepository seatRepository;
    
    // @Autowired
    // private BookingRepository bookingRepository; // Will be used for future booking integration
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Add user to waitlist
     */
    public WaitlistResponse addToWaitlist(Long userId, WaitlistRequest request) {
        logger.info("Adding user {} to waitlist for train: {}, coach: {}", userId, request.getTrainId(), request.getCoachId());
        
        try {
            // Validate entities
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            
            Train train = trainRepository.findById(request.getTrainId())
                .orElseThrow(() -> new RuntimeException("Train not found with ID: " + request.getTrainId()));
            
            Coach coach = coachRepository.findById(request.getCoachId())
                .orElseThrow(() -> new RuntimeException("Coach not found with ID: " + request.getCoachId()));
            
            // Check if user already has a waitlist entry for this train/coach/date
            List<WaitlistEntry> existingEntries = waitlistRepository.findByUser(user).stream()
                .filter(entry -> entry.getTrain().getId().equals(request.getTrainId()) &&
                               entry.getCoach().getId().equals(request.getCoachId()) &&
                               entry.getJourneyDate().toLocalDate().equals(request.getJourneyDate().toLocalDate()) &&
                               entry.getStatus() == WaitlistEntry.WaitlistStatus.PENDING)
                .collect(Collectors.toList());
            
            if (!existingEntries.isEmpty()) {
                throw new RuntimeException("User already has a pending waitlist entry for this train/coach/date");
            }
            
            // Get next waitlist number
            Integer nextWaitlistNumber = getNextWaitlistNumber(train, request.getJourneyDate(), request.getQuotaType());
            
            // Create waitlist entry
            WaitlistEntry waitlistEntry = new WaitlistEntry();
            waitlistEntry.setUser(user);
            waitlistEntry.setTrain(train);
            waitlistEntry.setCoach(coach);
            waitlistEntry.setJourneyDate(request.getJourneyDate());
            waitlistEntry.setWaitlistNumber(nextWaitlistNumber);
            waitlistEntry.setStatus(WaitlistEntry.WaitlistStatus.PENDING);
            waitlistEntry.setQuotaType(request.getQuotaType());
            waitlistEntry.setPassengerCount(request.getPassengerCount());
            waitlistEntry.setPreferredBerthType(request.getPreferredBerthType());
            waitlistEntry.setPreferredSeatType(request.getPreferredSeatType());
            waitlistEntry.setIsLadiesQuota(request.getIsLadiesQuota());
            waitlistEntry.setIsSeniorCitizenQuota(request.getIsSeniorCitizenQuota());
            waitlistEntry.setIsHandicappedFriendly(request.getIsHandicappedFriendly());
            waitlistEntry.setAutoUpgradeEnabled(request.getAutoUpgradeEnabled());
            waitlistEntry.setPriorityScore(calculatePriorityScore(user, request));
            waitlistEntry.setExpiryTime(calculateExpiryTime(request.getJourneyDate()));
            
            waitlistEntry = waitlistRepository.save(waitlistEntry);
            
            // Create response
            WaitlistResponse response = convertToWaitlistResponse(waitlistEntry);
            response.setCurrentPosition(getCurrentPosition(waitlistEntry));
            response.setTotalWaitlistEntries(getTotalWaitlistEntries(train, request.getJourneyDate(), request.getQuotaType()));
            response.setEstimatedConfirmationChance(calculateConfirmationChance(waitlistEntry));
            response.setMessage("Successfully added to waitlist with number: " + nextWaitlistNumber);
            
            logger.info("User {} added to waitlist with number: {}", userId, nextWaitlistNumber);
            return response;
            
        } catch (Exception e) {
            logger.error("Error adding user to waitlist: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add to waitlist: " + e.getMessage());
        }
    }
    
    /**
     * Process waitlist and convert to RAC when seats become available
     */
    public void processWaitlistForTrain(Long trainId, LocalDateTime journeyDate) {
        logger.info("Processing waitlist for train: {}, date: {}", trainId, journeyDate);
        
        try {
            Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new RuntimeException("Train not found with ID: " + trainId));
            
            List<Coach> coaches = coachRepository.findByTrain(train);
            
            for (Coach coach : coaches) {
                processWaitlistForCoach(coach, journeyDate);
            }
            
        } catch (Exception e) {
            logger.error("Error processing waitlist: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Process waitlist for a specific coach
     */
    private void processWaitlistForCoach(Coach coach, LocalDateTime journeyDate) {
        logger.info("Processing waitlist for coach: {}, date: {}", coach.getCoachNumber(), journeyDate);
        
        // Get available seats
        List<Seat> availableSeats = seatRepository.findAvailableSeatsByCoach(coach);
        
        // Get pending waitlist entries
        List<WaitlistEntry> pendingEntries = waitlistRepository.findPendingWaitlistByCoachAndDate(coach, journeyDate);
        
        // Get existing RAC entries
        List<RacEntry> existingRacEntries = racRepository.findActiveRacByCoachAndDate(coach, journeyDate);
        
        // Calculate how many seats can be allocated for RAC
        int racCapacity = calculateRacCapacity(coach, availableSeats.size(), existingRacEntries.size());
        
        // Convert waitlist entries to RAC
        int convertedCount = 0;
        for (WaitlistEntry entry : pendingEntries) {
            if (convertedCount >= racCapacity) {
                break;
            }
            
            if (convertWaitlistToRac(entry, coach, journeyDate)) {
                convertedCount++;
            }
        }
        
        logger.info("Converted {} waitlist entries to RAC for coach: {}", convertedCount, coach.getCoachNumber());
    }
    
    /**
     * Convert waitlist entry to RAC
     */
    private boolean convertWaitlistToRac(WaitlistEntry waitlistEntry, Coach coach, LocalDateTime journeyDate) {
        try {
            // Find available seat for RAC
            Optional<Seat> availableSeat = findAvailableSeatForRac(coach, waitlistEntry);
            
            if (availableSeat.isPresent()) {
                // Get next RAC number
                Integer nextRacNumber = getNextRacNumber(waitlistEntry.getTrain(), journeyDate, waitlistEntry.getQuotaType());
                
                // Create RAC entry
                RacEntry racEntry = new RacEntry();
                racEntry.setUser(waitlistEntry.getUser());
                racEntry.setTrain(waitlistEntry.getTrain());
                racEntry.setCoach(waitlistEntry.getCoach());
                racEntry.setSeat(availableSeat.get());
                racEntry.setJourneyDate(journeyDate);
                racEntry.setRacNumber(nextRacNumber);
                racEntry.setStatus(RacEntry.RacStatus.RAC);
                racEntry.setQuotaType(convertQuotaType(waitlistEntry.getQuotaType()));
                racEntry.setPassengerCount(waitlistEntry.getPassengerCount());
                racEntry.setBerthType(waitlistEntry.getPreferredBerthType());
                racEntry.setSeatType(waitlistEntry.getPreferredSeatType());
                racEntry.setIsLadiesQuota(waitlistEntry.getIsLadiesQuota());
                racEntry.setIsSeniorCitizenQuota(waitlistEntry.getIsSeniorCitizenQuota());
                racEntry.setIsHandicappedFriendly(waitlistEntry.getIsHandicappedFriendly());
                racEntry.setAutoUpgradeEnabled(waitlistEntry.getAutoUpgradeEnabled());
                racEntry.setPriorityScore(waitlistEntry.getPriorityScore());
                
                racRepository.save(racEntry);
                
                // Update waitlist entry status
                waitlistEntry.setStatus(WaitlistEntry.WaitlistStatus.RAC);
                waitlistRepository.save(waitlistEntry);
                
                logger.info("Converted waitlist entry {} to RAC {} for user: {}", 
                           waitlistEntry.getWaitlistNumber(), nextRacNumber, waitlistEntry.getUser().getUsername());
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error converting waitlist to RAC: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get user's waitlist entries
     */
    public List<WaitlistResponse> getUserWaitlistEntries(Long userId) {
        logger.info("Getting waitlist entries for user: {}", userId);
        
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            
            List<WaitlistEntry> entries = waitlistRepository.findByUser(user);
            
            return entries.stream()
                .map(this::convertToWaitlistResponse)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            logger.error("Error getting user waitlist entries: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get waitlist entries: " + e.getMessage());
        }
    }
    
    /**
     * Cancel waitlist entry
     */
    public boolean cancelWaitlistEntry(Long userId, Long waitlistId) {
        logger.info("Cancelling waitlist entry: {} for user: {}", waitlistId, userId);
        
        try {
            WaitlistEntry entry = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found with ID: " + waitlistId));
            
            if (!entry.getUser().getId().equals(userId)) {
                throw new RuntimeException("User not authorized to cancel this waitlist entry");
            }
            
            if (entry.getStatus() != WaitlistEntry.WaitlistStatus.PENDING) {
                throw new RuntimeException("Cannot cancel waitlist entry with status: " + entry.getStatus());
            }
            
            entry.setStatus(WaitlistEntry.WaitlistStatus.CANCELLED);
            entry.setCancelledAt(LocalDateTime.now());
            entry.setCancellationReason("Cancelled by user");
            
            waitlistRepository.save(entry);
            
            logger.info("Successfully cancelled waitlist entry: {}", waitlistId);
            return true;
            
        } catch (Exception e) {
            logger.error("Error cancelling waitlist entry: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get next waitlist number
     */
    private Integer getNextWaitlistNumber(Train train, LocalDateTime journeyDate, WaitlistEntry.QuotaType quotaType) {
        Optional<Integer> maxNumber = waitlistRepository.findMaxWaitlistNumberByTrainDateAndQuota(train, journeyDate, quotaType);
        return maxNumber.orElse(0) + 1;
    }
    
    /**
     * Get next RAC number
     */
    private Integer getNextRacNumber(Train train, LocalDateTime journeyDate, WaitlistEntry.QuotaType quotaType) {
        Optional<Integer> maxNumber = racRepository.findMaxRacNumberByTrainDateAndQuota(train, journeyDate, convertQuotaType(quotaType));
        return maxNumber.orElse(0) + 1;
    }
    
    /**
     * Calculate priority score
     */
    private Integer calculatePriorityScore(User user, WaitlistRequest request) {
        int score = 0;
        
        // Base score
        score += 100;
        
        // Senior citizen bonus
        if (request.getIsSeniorCitizenQuota()) {
            score += 50;
        }
        
        // Ladies quota bonus
        if (request.getIsLadiesQuota()) {
            score += 30;
        }
        
        // Handicapped friendly bonus
        if (request.getIsHandicappedFriendly()) {
            score += 40;
        }
        
        // Tatkal quota penalty
        if (request.getQuotaType() == WaitlistEntry.QuotaType.TATKAL) {
            score -= 20;
        }
        
        return score;
    }
    
    /**
     * Calculate expiry time
     */
    private LocalDateTime calculateExpiryTime(LocalDateTime journeyDate) {
        // Waitlist expires 4 hours before journey
        return journeyDate.minusHours(4);
    }
    
    /**
     * Calculate RAC capacity
     */
    private int calculateRacCapacity(Coach coach, int availableSeats, int existingRacCount) {
        // RAC capacity is typically 20% of total seats
        int maxRacCapacity = (int) (coach.getTotalSeats() * 0.2);
        return Math.max(0, maxRacCapacity - existingRacCount);
    }
    
    /**
     * Find available seat for RAC
     */
    private Optional<Seat> findAvailableSeatForRac(Coach coach, WaitlistEntry waitlistEntry) {
        List<Seat> availableSeats = seatRepository.findAvailableSeatsByCoach(coach);
        
        // Filter by preferences if specified
        if (waitlistEntry.getPreferredSeatType() != null) {
            availableSeats = availableSeats.stream()
                .filter(seat -> seat.getSeatType().name().equals(waitlistEntry.getPreferredSeatType()))
                .collect(Collectors.toList());
        }
        
        if (waitlistEntry.getPreferredBerthType() != null) {
            availableSeats = availableSeats.stream()
                .filter(seat -> seat.getBerthType().name().equals(waitlistEntry.getPreferredBerthType()))
                .collect(Collectors.toList());
        }
        
        return availableSeats.stream().findFirst();
    }
    
    /**
     * Convert quota type
     */
    private RacEntry.QuotaType convertQuotaType(WaitlistEntry.QuotaType quotaType) {
        return RacEntry.QuotaType.valueOf(quotaType.name());
    }
    
    /**
     * Convert to waitlist response
     */
    private WaitlistResponse convertToWaitlistResponse(WaitlistEntry entry) {
        WaitlistResponse response = new WaitlistResponse();
        response.setId(entry.getId());
        response.setTrainId(entry.getTrain().getId());
        response.setTrainNumber(entry.getTrain().getTrainNumber());
        response.setTrainName(entry.getTrain().getTrainName());
        response.setCoachId(entry.getCoach().getId());
        response.setCoachNumber(entry.getCoach().getCoachNumber());
        response.setCoachType(entry.getCoach().getCoachType().name());
        response.setJourneyDate(entry.getJourneyDate());
        response.setWaitlistNumber(entry.getWaitlistNumber());
        response.setStatus(entry.getStatus());
        response.setQuotaType(entry.getQuotaType());
        response.setPassengerCount(entry.getPassengerCount());
        response.setPreferredBerthType(entry.getPreferredBerthType());
        response.setPreferredSeatType(entry.getPreferredSeatType());
        response.setIsLadiesQuota(entry.getIsLadiesQuota());
        response.setIsSeniorCitizenQuota(entry.getIsSeniorCitizenQuota());
        response.setIsHandicappedFriendly(entry.getIsHandicappedFriendly());
        response.setPriorityScore(entry.getPriorityScore());
        response.setAutoUpgradeEnabled(entry.getAutoUpgradeEnabled());
        response.setNotificationSent(entry.getNotificationSent());
        response.setExpiryTime(entry.getExpiryTime());
        response.setCreatedAt(entry.getCreatedAt());
        response.setUpdatedAt(entry.getUpdatedAt());
        
        return response;
    }
    
    /**
     * Get current position in waitlist
     */
    private Integer getCurrentPosition(WaitlistEntry entry) {
        return entry.getWaitlistNumber();
    }
    
    /**
     * Get total waitlist entries
     */
    private Integer getTotalWaitlistEntries(Train train, LocalDateTime journeyDate, WaitlistEntry.QuotaType quotaType) {
        return waitlistRepository.countPendingWaitlistByTrainAndDate(train, journeyDate).intValue();
    }
    
    /**
     * Calculate confirmation chance
     */
    private Integer calculateConfirmationChance(WaitlistEntry entry) {
        // Simple calculation based on waitlist position
        int totalSeats = entry.getCoach().getTotalSeats();
        int waitlistPosition = entry.getWaitlistNumber();
        
        if (waitlistPosition <= totalSeats) {
            return 90; // High chance
        } else if (waitlistPosition <= totalSeats * 1.5) {
            return 60; // Medium chance
        } else if (waitlistPosition <= totalSeats * 2) {
            return 30; // Low chance
        } else {
            return 10; // Very low chance
        }
    }
}
