package com.irctc_backend.irctc.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

/**
 * Event published when a ticket gets confirmed from RAC or Waitlist
 * This event triggers notifications to passengers about their confirmed tickets
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketConfirmationEvent {
    
    // User Information
    private Long userId;
    private String passengerName;
    private String passengerEmail;
    private String passengerPhone;
    
    // Train Information
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private String sourceStation;
    private String destinationStation;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    
    // Journey Information
    private LocalDate journeyDate;
    private String pnrNumber;
    private BigDecimal fare;
    private String quotaType;
    
    // Seat Information
    private String seatNumber;
    private String coachNumber;
    private String coachType;
    private String berthType;
    private String seatType;
    
    // Confirmation Details
    private LocalDateTime confirmationTime;
    private String previousStatus; // RAC, WAITLIST
    private Integer racNumber;
    private Integer waitlistNumber;
    
    // Additional Information
    private String confirmationReason; // CANCELLATION, CHART_PREPARATION, MANUAL
    private Boolean autoUpgradeEnabled;
    private Integer priorityScore;
    
    // Notification Preferences
    private Boolean emailNotificationEnabled;
    private Boolean smsNotificationEnabled;
    private Boolean pushNotificationEnabled;
    
    // Metadata
    private String requestId;
    private LocalDateTime eventTimestamp;
    private String eventSource; // BATCH_JOB, MANUAL, API
    
    /**
     * Create a TicketConfirmationEvent from RAC entry
     */
    public static TicketConfirmationEvent fromRacEntry(com.irctc_backend.irctc.entity.RacEntry racEntry, 
                                                      com.irctc_backend.irctc.entity.Seat seat,
                                                      String pnrNumber) {
        return TicketConfirmationEvent.builder()
            .userId(racEntry.getUser().getId())
            .passengerName(racEntry.getUser().getFirstName() + " " + racEntry.getUser().getLastName())
            .passengerEmail(racEntry.getUser().getEmail())
            .passengerPhone(racEntry.getUser().getPhoneNumber())
            .trainId(racEntry.getTrain().getId())
            .trainNumber(racEntry.getTrain().getTrainNumber())
            .trainName(racEntry.getTrain().getTrainName())
            .sourceStation(racEntry.getTrain().getSourceStation().getStationName())
            .destinationStation(racEntry.getTrain().getDestinationStation().getStationName())
            .departureTime(racEntry.getTrain().getDepartureTime())
            .arrivalTime(racEntry.getTrain().getArrivalTime())
            .journeyDate(racEntry.getJourneyDate().toLocalDate())
            .pnrNumber(pnrNumber)
            .seatNumber(seat.getSeatNumber())
            .coachNumber(racEntry.getCoach().getCoachNumber())
            .coachType(racEntry.getCoach().getCoachType().name())
            .berthType(racEntry.getBerthType())
            .seatType(racEntry.getSeatType())
            .quotaType(racEntry.getQuotaType().name())
            .confirmationTime(LocalDateTime.now())
            .previousStatus("RAC")
            .racNumber(racEntry.getRacNumber())
            .autoUpgradeEnabled(racEntry.getAutoUpgradeEnabled())
            .priorityScore(racEntry.getPriorityScore())
            .emailNotificationEnabled(true)
            .smsNotificationEnabled(true)
            .pushNotificationEnabled(true)
            .eventTimestamp(LocalDateTime.now())
            .eventSource("BATCH_JOB")
            .confirmationReason("CANCELLATION")
            .build();
    }
    
    /**
     * Create a TicketConfirmationEvent from Waitlist entry
     */
    public static TicketConfirmationEvent fromWaitlistEntry(com.irctc_backend.irctc.entity.WaitlistEntry waitlistEntry,
                                                           com.irctc_backend.irctc.entity.Seat seat,
                                                           String pnrNumber) {
        return TicketConfirmationEvent.builder()
            .userId(waitlistEntry.getUser().getId())
            .passengerName(waitlistEntry.getUser().getFirstName() + " " + waitlistEntry.getUser().getLastName())
            .passengerEmail(waitlistEntry.getUser().getEmail())
            .passengerPhone(waitlistEntry.getUser().getPhoneNumber())
            .trainId(waitlistEntry.getTrain().getId())
            .trainNumber(waitlistEntry.getTrain().getTrainNumber())
            .trainName(waitlistEntry.getTrain().getTrainName())
            .sourceStation(waitlistEntry.getTrain().getSourceStation().getStationName())
            .destinationStation(waitlistEntry.getTrain().getDestinationStation().getStationName())
            .departureTime(waitlistEntry.getTrain().getDepartureTime())
            .arrivalTime(waitlistEntry.getTrain().getArrivalTime())
            .journeyDate(waitlistEntry.getJourneyDate().toLocalDate())
            .pnrNumber(pnrNumber)
            .seatNumber(seat.getSeatNumber())
            .coachNumber(waitlistEntry.getCoach().getCoachNumber())
            .coachType(waitlistEntry.getCoach().getCoachType().name())
            .berthType(waitlistEntry.getPreferredBerthType())
            .seatType(waitlistEntry.getPreferredSeatType())
            .quotaType(waitlistEntry.getQuotaType().name())
            .confirmationTime(LocalDateTime.now())
            .previousStatus("WAITLIST")
            .waitlistNumber(waitlistEntry.getWaitlistNumber())
            .autoUpgradeEnabled(waitlistEntry.getAutoUpgradeEnabled())
            .priorityScore(waitlistEntry.getPriorityScore())
            .emailNotificationEnabled(true)
            .smsNotificationEnabled(true)
            .pushNotificationEnabled(true)
            .eventTimestamp(LocalDateTime.now())
            .eventSource("BATCH_JOB")
            .confirmationReason("CHART_PREPARATION")
            .build();
    }
    
    /**
     * Get notification data as Map for push notifications
     */
    public Map<String, Object> getNotificationData() {
        return Map.of(
            "pnr", pnrNumber,
            "trainNumber", trainNumber,
            "trainName", trainName,
            "seatNumber", seatNumber,
            "coachNumber", coachNumber,
            "journeyDate", journeyDate.toString(),
            "previousStatus", previousStatus,
            "confirmationTime", confirmationTime.toString(),
            "fare", fare != null ? fare.toString() : "0"
        );
    }
    
    /**
     * Get SMS message content
     */
    public String getSmsMessage() {
        return String.format(
            "🎉 Your ticket is CONFIRMED! PNR: %s, Train: %s, Seat: %s-%s, Date: %s. Safe journey!",
            pnrNumber,
            trainNumber,
            coachNumber,
            seatNumber,
            journeyDate
        );
    }
    
    /**
     * Get email subject
     */
    public String getEmailSubject() {
        return String.format("🎉 Your Ticket is Confirmed! PNR: %s", pnrNumber);
    }
}
