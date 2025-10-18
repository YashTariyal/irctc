package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.dto.SeatSelectionRequest;
import com.irctc_backend.irctc.dto.SeatSelectionResponse;
import com.irctc_backend.irctc.entity.*;
import com.irctc_backend.irctc.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for seat selection functionality
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
@Transactional
public class SeatSelectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SeatSelectionService.class);
    
    @Autowired
    private TrainRepository trainRepository;
    
    @Autowired
    private CoachRepository coachRepository;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    /**
     * Get available seats for a specific train and coach
     */
    public SeatSelectionResponse getAvailableSeats(Long trainId, Long coachId, String journeyDate) {
        logger.info("Getting available seats for train: {}, coach: {}, date: {}", trainId, coachId, journeyDate);
        
        try {
            // Validate train exists
            Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new RuntimeException("Train not found with ID: " + trainId));
            
            // Validate coach exists and belongs to train
            Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new RuntimeException("Coach not found with ID: " + coachId));
            
            if (!coach.getTrain().getId().equals(trainId)) {
                throw new RuntimeException("Coach does not belong to the specified train");
            }
            
            // Parse journey date
            LocalDate journeyDateParsed = LocalDate.parse(journeyDate, DateTimeFormatter.ISO_LOCAL_DATE);
            
            // Get all seats for the coach
            List<Seat> allSeats = seatRepository.findByCoach(coach);
            
            // Get booked seats for the journey date
            List<Booking> bookings = bookingRepository.findBookingsByTrainAndDate(train, journeyDateParsed);
            Set<Long> bookedSeatIds = bookings.stream()
                .map(booking -> booking.getSeat().getId())
                .collect(Collectors.toSet());
            
            // Filter available seats
            List<Seat> availableSeats = allSeats.stream()
                .filter(seat -> !bookedSeatIds.contains(seat.getId()) && 
                               seat.getStatus() == Seat.SeatStatus.AVAILABLE)
                .collect(Collectors.toList());
            
            // Convert to SeatInfo DTOs
            List<SeatSelectionResponse.SeatInfo> seatInfos = availableSeats.stream()
                .map(this::convertToSeatInfo)
                .collect(Collectors.toList());
            
            // Create response
            SeatSelectionResponse response = new SeatSelectionResponse();
            response.setTrainId(trainId);
            response.setTrainNumber(train.getTrainNumber());
            response.setTrainName(train.getTrainName());
            response.setCoachId(coachId);
            response.setCoachNumber(coach.getCoachNumber());
            response.setCoachType(coach.getCoachType());
            response.setJourneyDate(journeyDate);
            response.setAvailableSeats(seatInfos);
            response.setTotalSeats(allSeats.size());
            response.setAvailableSeatsCount(availableSeats.size());
            response.setSelectionStatus("AVAILABLE");
            response.setMessage("Seats retrieved successfully");
            
            logger.info("Found {} available seats out of {} total seats", availableSeats.size(), allSeats.size());
            return response;
            
        } catch (Exception e) {
            logger.error("Error getting available seats: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get available seats: " + e.getMessage());
        }
    }
    
    /**
     * Select seats based on preferences
     */
    public SeatSelectionResponse selectSeats(SeatSelectionRequest request) {
        logger.info("Selecting seats for train: {}, coach: {}, preferences: {}", 
                   request.getTrainId(), request.getCoachId(), request.getSeatPreferences().size());
        
        try {
            // Get available seats first
            SeatSelectionResponse availableSeats = getAvailableSeats(
                request.getTrainId(), request.getCoachId(), request.getJourneyDate());
            
            if (availableSeats.getAvailableSeatsCount() == 0) {
                availableSeats.setSelectionStatus("NO_SEATS_AVAILABLE");
                availableSeats.setMessage("No seats available for the selected criteria");
                return availableSeats;
            }
            
            // Find matching seats based on preferences
            List<SeatSelectionResponse.SeatInfo> selectedSeats = new ArrayList<>();
            BigDecimal totalFare = BigDecimal.ZERO;
            
            for (SeatSelectionRequest.SeatPreference preference : request.getSeatPreferences()) {
                SeatSelectionResponse.SeatInfo matchingSeat = findMatchingSeat(
                    availableSeats.getAvailableSeats(), preference);
                
                if (matchingSeat != null) {
                    selectedSeats.add(matchingSeat);
                    totalFare = totalFare.add(matchingSeat.getFare());
                }
            }
            
            // Update response with selected seats
            availableSeats.setSelectedSeats(selectedSeats);
            availableSeats.setTotalFare(totalFare);
            
            if (selectedSeats.size() == request.getSeatPreferences().size()) {
                availableSeats.setSelectionStatus("ALL_SEATS_SELECTED");
                availableSeats.setMessage("All requested seats have been selected successfully");
            } else if (selectedSeats.size() > 0) {
                availableSeats.setSelectionStatus("PARTIAL_SELECTION");
                availableSeats.setMessage("Some seats selected. " + 
                    (request.getSeatPreferences().size() - selectedSeats.size()) + " seats not available");
            } else {
                availableSeats.setSelectionStatus("NO_MATCHING_SEATS");
                availableSeats.setMessage("No seats match the specified preferences");
            }
            
            logger.info("Selected {} seats out of {} requested", selectedSeats.size(), request.getSeatPreferences().size());
            return availableSeats;
            
        } catch (Exception e) {
            logger.error("Error selecting seats: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to select seats: " + e.getMessage());
        }
    }
    
    /**
     * Get coach layout for visual representation
     */
    public Map<String, Object> getCoachLayout(Long coachId) {
        logger.info("Getting coach layout for coach: {}", coachId);
        
        try {
            Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new RuntimeException("Coach not found with ID: " + coachId));
            
            List<Seat> seats = seatRepository.findByCoach(coach);
            
            // Group seats by row/compartment for layout
            Map<String, List<SeatSelectionResponse.SeatInfo>> layout = new HashMap<>();
            
            for (Seat seat : seats) {
                String rowKey = getRowKey(seat.getSeatNumber());
                layout.computeIfAbsent(rowKey, k -> new ArrayList<>())
                      .add(convertToSeatInfo(seat));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("coachId", coachId);
            response.put("coachNumber", coach.getCoachNumber());
            response.put("coachType", coach.getCoachType());
            response.put("totalSeats", coach.getTotalSeats());
            response.put("layout", layout);
            response.put("layoutType", getLayoutType(coach.getCoachType()));
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error getting coach layout: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get coach layout: " + e.getMessage());
        }
    }
    
    /**
     * Convert Seat entity to SeatInfo DTO
     */
    private SeatSelectionResponse.SeatInfo convertToSeatInfo(Seat seat) {
        SeatSelectionResponse.SeatInfo seatInfo = new SeatSelectionResponse.SeatInfo();
        seatInfo.setSeatId(seat.getId());
        seatInfo.setSeatNumber(seat.getSeatNumber());
        seatInfo.setBerthNumber(seat.getBerthNumber());
        seatInfo.setSeatType(seat.getSeatType());
        seatInfo.setBerthType(seat.getBerthType());
        seatInfo.setStatus(seat.getStatus());
        seatInfo.setIsLadiesQuota(seat.getIsLadiesQuota());
        seatInfo.setIsSeniorCitizenQuota(seat.getIsSeniorCitizenQuota());
        seatInfo.setIsHandicappedFriendly(seat.getIsHandicappedFriendly());
        
        // Calculate fare based on coach type and seat type
        seatInfo.setFare(calculateSeatFare(seat));
        
        // Set position and color for UI
        seatInfo.setPosition(getSeatPosition(seat.getSeatType()));
        seatInfo.setColor(getSeatColor(seat.getStatus()));
        
        return seatInfo;
    }
    
    /**
     * Find matching seat based on preferences
     */
    private SeatSelectionResponse.SeatInfo findMatchingSeat(
            List<SeatSelectionResponse.SeatInfo> availableSeats, 
            SeatSelectionRequest.SeatPreference preference) {
        
        return availableSeats.stream()
            .filter(seat -> seat.getSeatType() == preference.getSeatType() &&
                           seat.getBerthType() == preference.getBerthType() &&
                           seat.getStatus() == Seat.SeatStatus.AVAILABLE &&
                           (!preference.getIsLadiesQuota() || seat.getIsLadiesQuota()) &&
                           (!preference.getIsSeniorCitizenQuota() || seat.getIsSeniorCitizenQuota()) &&
                           (!preference.getIsHandicappedFriendly() || seat.getIsHandicappedFriendly()))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Calculate seat fare
     */
    private BigDecimal calculateSeatFare(Seat seat) {
        // Base fare calculation logic
        BigDecimal baseFare = BigDecimal.valueOf(100); // Default base fare
        
        // Adjust based on seat type
        switch (seat.getSeatType()) {
            case WINDOW:
                baseFare = baseFare.add(BigDecimal.valueOf(50));
                break;
            case AISLE:
                baseFare = baseFare.add(BigDecimal.valueOf(25));
                break;
            case MIDDLE:
                baseFare = baseFare.subtract(BigDecimal.valueOf(25));
                break;
            case SIDE_UPPER:
                baseFare = baseFare.subtract(BigDecimal.valueOf(50));
                break;
            case SIDE_LOWER:
                baseFare = baseFare.add(BigDecimal.valueOf(25));
                break;
        }
        
        // Adjust based on berth type
        switch (seat.getBerthType()) {
            case LOWER:
                baseFare = baseFare.add(BigDecimal.valueOf(100));
                break;
            case MIDDLE:
                baseFare = baseFare.add(BigDecimal.valueOf(25));
                break;
            case UPPER:
                baseFare = baseFare.subtract(BigDecimal.valueOf(50));
                break;
            case SIDE_LOWER:
                baseFare = baseFare.add(BigDecimal.valueOf(75));
                break;
            case SIDE_UPPER:
                baseFare = baseFare.subtract(BigDecimal.valueOf(25));
                break;
        }
        
        return baseFare;
    }
    
    /**
     * Get row key for layout grouping
     */
    private String getRowKey(String seatNumber) {
        // Extract row number from seat number (e.g., "12" -> "1", "25" -> "2")
        if (seatNumber.length() >= 2) {
            return seatNumber.substring(0, seatNumber.length() - 1);
        }
        return "1";
    }
    
    /**
     * Get layout type based on coach type
     */
    private String getLayoutType(Coach.CoachType coachType) {
        switch (coachType) {
            case AC_FIRST_CLASS:
            case AC_2_TIER:
            case AC_3_TIER:
                return "berth";
            case SLEEPER_CLASS:
                return "berth";
            case AC_CHAIR_CAR:
            case EXECUTIVE_CHAIR_CAR:
            case SECOND_SITTING:
                return "chair";
            default:
                return "berth";
        }
    }
    
    /**
     * Get seat position for UI
     */
    private String getSeatPosition(Seat.SeatType seatType) {
        switch (seatType) {
            case WINDOW:
                return "window";
            case AISLE:
                return "aisle";
            case MIDDLE:
                return "middle";
            default:
                return "standard";
        }
    }
    
    /**
     * Get seat color based on status
     */
    private String getSeatColor(Seat.SeatStatus status) {
        switch (status) {
            case AVAILABLE:
                return "green";
            case BOOKED:
                return "red";
            case RESERVED:
                return "yellow";
            case MAINTENANCE:
                return "gray";
            case BLOCKED:
                return "black";
            default:
                return "white";
        }
    }
}
