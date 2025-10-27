package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.entity.*;
import com.irctc_backend.irctc.events.TicketConfirmationEvent;
import com.irctc_backend.irctc.repository.*;
import com.irctc_backend.irctc.util.LoggingUtil;
import com.irctc_backend.irctc.metrics.TicketConfirmationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service responsible for batch processing of ticket confirmations
 * Automatically converts RAC and Waitlist tickets to confirmed status
 * and publishes events for notification services
 */
@Service
@EnableScheduling
public class TicketConfirmationBatchService {
    
    private static final Logger logger = LoggerFactory.getLogger(TicketConfirmationBatchService.class);
    
    @Autowired
    private TrainRepository trainRepository;
    
    @Autowired
    private CoachRepository coachRepository;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private RacRepository racRepository;
    
    @Autowired
    private WaitlistRepository waitlistRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private TicketConfirmationMetrics metrics;
    
    /**
     * Main scheduled job that runs every 30 minutes to process ticket confirmations
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void processTicketConfirmations() {
        String requestId = LoggingUtil.generateRequestId();
        long startTime = System.currentTimeMillis();
        
        logger.info("Starting ticket confirmation batch processing - RequestId: {}", requestId);
        
        // Record metrics
        metrics.recordBatchProcessingRun();
        metrics.incrementActiveBatchJobs();
        
        try {
            // Get all active trains with upcoming journeys
            List<Train> upcomingTrains = getUpcomingTrains();
            logger.info("Found {} trains to process for confirmations", upcomingTrains.size());
            
            int totalConfirmations = 0;
            
            // Process each train
            for (Train train : upcomingTrains) {
                long trainStartTime = System.currentTimeMillis();
                int trainConfirmations = processTrainConfirmations(train, requestId);
                totalConfirmations += trainConfirmations;
                
                // Record train processing time
                long trainProcessingTime = System.currentTimeMillis() - trainStartTime;
                metrics.recordTrainProcessingTime(java.time.Duration.ofMillis(trainProcessingTime));
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Record batch processing time
            metrics.recordBatchProcessingTime(java.time.Duration.ofMillis(processingTime));
            
            logger.info("Completed ticket confirmation batch processing - RequestId: {}, " +
                       "Total confirmations: {}, Processing time: {}ms", 
                       requestId, totalConfirmations, processingTime);
            
            // Log batch processing metrics
            LoggingUtil.logBusinessOperation("BATCH_CONFIRMATION_PROCESSING", "BATCH_JOB", 
                                           requestId, "SYSTEM", 
                                           String.format("Processed %d confirmations in %dms", 
                                                        totalConfirmations, processingTime));
            
        } catch (Exception e) {
            logger.error("Error in ticket confirmation batch processing - RequestId: {}", requestId, e);
            metrics.recordBatchProcessingError();
            LoggingUtil.logError("BATCH_CONFIRMATION_ERROR", "BATCH_JOB", requestId, 
                               "SYSTEM", "Batch processing failed", e);
        } finally {
            metrics.decrementActiveBatchJobs();
        }
    }
    
    /**
     * Process confirmations for a specific train
     */
    @Transactional
    public int processTrainConfirmations(Train train, String requestId) {
        logger.info("Processing confirmations for train: {} - RequestId: {}", 
                   train.getTrainNumber(), requestId);
        
        int totalConfirmations = 0;
        
        try {
            // Get journey dates for next 7 days
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 7; i++) {
                LocalDate journeyDate = today.plusDays(i);
                int dateConfirmations = processJourneyDateConfirmations(train, journeyDate, requestId);
                totalConfirmations += dateConfirmations;
            }
            
            logger.info("Completed processing for train: {}, Total confirmations: {} - RequestId: {}", 
                       train.getTrainNumber(), totalConfirmations, requestId);
            
        } catch (Exception e) {
            logger.error("Error processing confirmations for train: {} - RequestId: {}", 
                        train.getTrainNumber(), requestId, e);
        }
        
        return totalConfirmations;
    }
    
    /**
     * Process confirmations for specific train and journey date
     */
    @Transactional
    public int processJourneyDateConfirmations(Train train, LocalDate journeyDate, String requestId) {
        logger.info("Processing confirmations for train: {}, date: {} - RequestId: {}", 
                   train.getTrainNumber(), journeyDate, requestId);
        
        int totalConfirmations = 0;
        
        try {
            // Get all coaches for this train
            List<Coach> coaches = coachRepository.findByTrain(train);
            
            for (Coach coach : coaches) {
                int coachConfirmations = processCoachConfirmations(train, coach, journeyDate, requestId);
                totalConfirmations += coachConfirmations;
            }
            
        } catch (Exception e) {
            logger.error("Error processing confirmations for train: {}, date: {} - RequestId: {}", 
                        train.getTrainNumber(), journeyDate, requestId, e);
        }
        
        return totalConfirmations;
    }
    
    /**
     * Process confirmations for specific coach
     */
    @Transactional
    public int processCoachConfirmations(Train train, Coach coach, LocalDate journeyDate, String requestId) {
        logger.info("Processing confirmations for coach: {}, train: {}, date: {} - RequestId: {}", 
                   coach.getCoachNumber(), train.getTrainNumber(), journeyDate, requestId);
        
        int confirmations = 0;
        
        try {
            LocalDateTime journeyDateTime = journeyDate.atStartOfDay();
            
            // Get available seats (cancelled seats)
            List<Seat> availableSeats = seatRepository.findAvailableSeatsByCoach(coach);
            
            // Get RAC entries for this coach and date, sorted by priority
            List<RacEntry> racEntries = racRepository.findActiveRacByCoachAndDate(coach, journeyDateTime)
                .stream()
                .sorted(Comparator.comparing(RacEntry::getRacNumber))
                .collect(Collectors.toList());
            
            // Get Waitlist entries for this coach and date, sorted by priority
            List<WaitlistEntry> waitlistEntries = waitlistRepository.findPendingWaitlistByCoachAndDate(coach, journeyDateTime)
                .stream()
                .sorted(Comparator.comparing(WaitlistEntry::getWaitlistNumber))
                .collect(Collectors.toList());
            
            logger.info("Found {} available seats, {} RAC entries, {} waitlist entries for coach: {} - RequestId: {}", 
                       availableSeats.size(), racEntries.size(), waitlistEntries.size(), 
                       coach.getCoachNumber(), requestId);
            
            // Process RAC to Confirmed conversions first
            for (Seat seat : availableSeats) {
                if (!racEntries.isEmpty()) {
                    RacEntry racEntry = racEntries.remove(0); // Get highest priority RAC
                    
                    if (convertRacToConfirmed(racEntry, seat, requestId)) {
                        confirmations++;
                    }
                } else if (!waitlistEntries.isEmpty()) {
                    // If no RAC entries, convert waitlist to confirmed
                    WaitlistEntry waitlistEntry = waitlistEntries.remove(0); // Get highest priority waitlist
                    
                    if (convertWaitlistToConfirmed(waitlistEntry, seat, requestId)) {
                        confirmations++;
                    }
                }
            }
            
            logger.info("Completed processing for coach: {}, confirmations: {} - RequestId: {}", 
                       coach.getCoachNumber(), confirmations, requestId);
            
        } catch (Exception e) {
            logger.error("Error processing confirmations for coach: {} - RequestId: {}", 
                        coach.getCoachNumber(), requestId, e);
        }
        
        return confirmations;
    }
    
    /**
     * Convert RAC entry to confirmed booking
     */
    @Transactional
    public boolean convertRacToConfirmed(RacEntry racEntry, Seat seat, String requestId) {
        try {
            logger.info("Converting RAC {} to CONFIRMED for user: {} - RequestId: {}", 
                       racEntry.getRacNumber(), racEntry.getUser().getId(), requestId);
            
            // Update RAC entry status
            racEntry.setStatus(RacEntry.RacStatus.CONFIRMED);
            racEntry.setConfirmedAt(LocalDateTime.now());
            racEntry.setSeat(seat);
            
            // Update seat status
            seat.setStatus(Seat.SeatStatus.BOOKED);
            
            // Save changes
            racRepository.save(racEntry);
            seatRepository.save(seat);
            
            // Create confirmed booking
            Booking confirmedBooking = createConfirmedBookingFromRac(racEntry, seat);
            bookingRepository.save(confirmedBooking);
            
            // Publish confirmation event
            publishConfirmationEvent(racEntry, seat, confirmedBooking.getPnrNumber(), requestId);
            
            // Record metrics
            metrics.recordConfirmation("RAC");
            
            logger.info("Successfully converted RAC {} to CONFIRMED for user: {} - RequestId: {}", 
                       racEntry.getRacNumber(), racEntry.getUser().getId(), requestId);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error converting RAC {} to confirmed - RequestId: {}", 
                        racEntry.getRacNumber(), requestId, e);
            return false;
        }
    }
    
    /**
     * Convert Waitlist entry to confirmed booking
     */
    @Transactional
    public boolean convertWaitlistToConfirmed(WaitlistEntry waitlistEntry, Seat seat, String requestId) {
        try {
            logger.info("Converting Waitlist {} to CONFIRMED for user: {} - RequestId: {}", 
                       waitlistEntry.getWaitlistNumber(), waitlistEntry.getUser().getId(), requestId);
            
            // Update waitlist entry status
            waitlistEntry.setStatus(WaitlistEntry.WaitlistStatus.CONFIRMED);
            
            // Update seat status
            seat.setStatus(Seat.SeatStatus.BOOKED);
            
            // Save changes
            waitlistRepository.save(waitlistEntry);
            seatRepository.save(seat);
            
            // Create confirmed booking
            Booking confirmedBooking = createConfirmedBookingFromWaitlist(waitlistEntry, seat);
            bookingRepository.save(confirmedBooking);
            
            // Publish confirmation event
            publishConfirmationEvent(waitlistEntry, seat, confirmedBooking.getPnrNumber(), requestId);
            
            // Record metrics
            metrics.recordConfirmation("WAITLIST");
            
            logger.info("Successfully converted Waitlist {} to CONFIRMED for user: {} - RequestId: {}", 
                       waitlistEntry.getWaitlistNumber(), waitlistEntry.getUser().getId(), requestId);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error converting Waitlist {} to confirmed - RequestId: {}", 
                        waitlistEntry.getWaitlistNumber(), requestId, e);
            return false;
        }
    }
    
    /**
     * Create confirmed booking from RAC entry
     */
    private Booking createConfirmedBookingFromRac(RacEntry racEntry, Seat seat) {
        Booking booking = new Booking();
        booking.setPnrNumber(generatePNR());
        booking.setUser(racEntry.getUser());
        booking.setTrain(racEntry.getTrain());
        booking.setSeat(seat);
        booking.setCoach(racEntry.getCoach());
        booking.setJourneyDate(racEntry.getJourneyDate().toLocalDate());
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.COMPLETED); // Assume payment was already done
        booking.setQuotaType(Booking.QuotaType.valueOf(racEntry.getQuotaType().name()));
        booking.setIsTatkal(false);
        booking.setIsCancelled(false);
        
        // Calculate fare
        BigDecimal fare = calculateFare(racEntry.getTrain(), racEntry.getCoach(), racEntry.getJourneyDate().toLocalDate());
        booking.setTotalFare(fare);
        booking.setBaseFare(fare);
        
        return booking;
    }
    
    /**
     * Create confirmed booking from Waitlist entry
     */
    private Booking createConfirmedBookingFromWaitlist(WaitlistEntry waitlistEntry, Seat seat) {
        Booking booking = new Booking();
        booking.setPnrNumber(generatePNR());
        booking.setUser(waitlistEntry.getUser());
        booking.setTrain(waitlistEntry.getTrain());
        booking.setSeat(seat);
        booking.setCoach(waitlistEntry.getCoach());
        booking.setJourneyDate(waitlistEntry.getJourneyDate().toLocalDate());
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.COMPLETED); // Assume payment was already done
        booking.setQuotaType(Booking.QuotaType.valueOf(waitlistEntry.getQuotaType().name()));
        booking.setIsTatkal(false);
        booking.setIsCancelled(false);
        
        // Calculate fare
        BigDecimal fare = calculateFare(waitlistEntry.getTrain(), waitlistEntry.getCoach(), waitlistEntry.getJourneyDate().toLocalDate());
        booking.setTotalFare(fare);
        booking.setBaseFare(fare);
        
        return booking;
    }
    
    /**
     * Publish confirmation event to Kafka
     */
    private void publishConfirmationEvent(RacEntry racEntry, Seat seat, String pnrNumber, String requestId) {
        try {
            TicketConfirmationEvent event = TicketConfirmationEvent.fromRacEntry(racEntry, seat, pnrNumber);
            event.setRequestId(requestId);
            
            kafkaTemplate.send("ticket-confirmation-events", event);
            
            // Record metrics
            metrics.recordKafkaEventPublished();
            
            logger.info("Published RAC confirmation event for user: {}, PNR: {} - RequestId: {}", 
                       event.getUserId(), event.getPnrNumber(), requestId);
            
            LoggingUtil.logKafkaEvent("TICKET_CONFIRMATION", "ticket-confirmation-events", 
                                   requestId, "SUCCESS");
            
        } catch (Exception e) {
            logger.error("Error publishing RAC confirmation event - RequestId: {}", requestId, e);
            metrics.recordKafkaEventFailed();
            LoggingUtil.logError("KAFKA_PUBLISH", "TICKET_CONFIRMATION", requestId, 
                               "SYSTEM", "Failed to publish RAC confirmation event", e);
        }
    }
    
    /**
     * Publish confirmation event to Kafka
     */
    private void publishConfirmationEvent(WaitlistEntry waitlistEntry, Seat seat, String pnrNumber, String requestId) {
        try {
            TicketConfirmationEvent event = TicketConfirmationEvent.fromWaitlistEntry(waitlistEntry, seat, pnrNumber);
            event.setRequestId(requestId);
            
            kafkaTemplate.send("ticket-confirmation-events", event);
            
            // Record metrics
            metrics.recordKafkaEventPublished();
            
            logger.info("Published Waitlist confirmation event for user: {}, PNR: {} - RequestId: {}", 
                       event.getUserId(), event.getPnrNumber(), requestId);
            
            LoggingUtil.logKafkaEvent("TICKET_CONFIRMATION", "ticket-confirmation-events", 
                                   requestId, "SUCCESS");
            
        } catch (Exception e) {
            logger.error("Error publishing Waitlist confirmation event - RequestId: {}", requestId, e);
            metrics.recordKafkaEventFailed();
            LoggingUtil.logError("KAFKA_PUBLISH", "TICKET_CONFIRMATION", requestId, 
                               "SYSTEM", "Failed to publish Waitlist confirmation event", e);
        }
    }
    
    /**
     * Get upcoming trains for processing
     */
    private List<Train> getUpcomingTrains() {
        return trainRepository.findAll()
            .stream()
            .filter(train -> train.getIsRunning() && train.getStatus().equals(Train.TrainStatus.ACTIVE))
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate fare for the booking
     */
    private BigDecimal calculateFare(Train train, Coach coach, LocalDate journeyDate) {
        try {
            // Simple fare calculation based on coach type
            BigDecimal baseFare = BigDecimal.valueOf(500);
            if (coach.getCoachType().name().contains("AC")) {
                baseFare = BigDecimal.valueOf(1000);
            }
            return baseFare;
        } catch (Exception e) {
            logger.warn("Error calculating fare, using default fare: {}", e.getMessage());
            return BigDecimal.valueOf(500); // Default fare
        }
    }
    
    /**
     * Generate PNR number
     */
    private String generatePNR() {
        Random random = new Random();
        int pnrNumber = 100000 + random.nextInt(900000);
        return "PNR" + pnrNumber;
    }
    
    /**
     * Manual trigger for processing specific train confirmations
     */
    public int processTrainConfirmationsManually(Long trainId) {
        String requestId = LoggingUtil.generateRequestId();
        logger.info("Manual trigger for train confirmations - TrainId: {}, RequestId: {}", trainId, requestId);
        
        try {
            Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new RuntimeException("Train not found with ID: " + trainId));
            
            return processTrainConfirmations(train, requestId);
            
        } catch (Exception e) {
            logger.error("Error in manual train confirmation processing - TrainId: {}, RequestId: {}", 
                        trainId, requestId, e);
            return 0;
        }
    }
    
    /**
     * Emergency batch processing for chart preparation (runs 4 hours before departure)
     */
    @Scheduled(cron = "0 0 */4 * * *") // Every 4 hours
    public void processChartPreparationConfirmations() {
        String requestId = LoggingUtil.generateRequestId();
        logger.info("Starting chart preparation confirmation processing - RequestId: {}", requestId);
        
        try {
            // Get trains departing in next 6 hours
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime sixHoursFromNow = now.plusHours(6);
            
            List<Train> departingTrains = trainRepository.findAll()
                .stream()
                .filter(train -> {
                    if (!train.getIsRunning()) return false;
                    LocalDateTime departureTime = LocalDateTime.of(LocalDate.now(), train.getDepartureTime());
                    return departureTime.isAfter(now) && departureTime.isBefore(sixHoursFromNow);
                })
                .collect(Collectors.toList());
            
            int totalConfirmations = 0;
            for (Train train : departingTrains) {
                int confirmations = processTrainConfirmations(train, requestId);
                totalConfirmations += confirmations;
            }
            
            logger.info("Completed chart preparation processing - RequestId: {}, Confirmations: {}", 
                       requestId, totalConfirmations);
            
        } catch (Exception e) {
            logger.error("Error in chart preparation processing - RequestId: {}", requestId, e);
        }
    }
}
