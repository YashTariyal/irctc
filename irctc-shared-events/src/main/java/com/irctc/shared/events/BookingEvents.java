package com.irctc.shared.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Booking-related events for Kafka messaging
 */
public class BookingEvents {

    public static class BookingCreatedEvent {
        @JsonProperty("eventId")
        private String eventId;
        
        @JsonProperty("bookingId")
        private Long bookingId;
        
        @JsonProperty("userId")
        private Long userId;
        
        @JsonProperty("trainId")
        private Long trainId;
        
        @JsonProperty("pnrNumber")
        private String pnrNumber;
        
        @JsonProperty("totalAmount")
        private BigDecimal totalAmount;
        
        @JsonProperty("passengerCount")
        private Integer passengerCount;
        
        @JsonProperty("journeyDate")
        private LocalDateTime journeyDate;
        
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
        
        @JsonProperty("eventType")
        private String eventType = "BOOKING_CREATED";

        // Constructors
        public BookingCreatedEvent() {}

        public BookingCreatedEvent(Long bookingId, Long userId, Long trainId, String pnrNumber, 
                                 BigDecimal totalAmount, Integer passengerCount, LocalDateTime journeyDate) {
            this.eventId = java.util.UUID.randomUUID().toString();
            this.bookingId = bookingId;
            this.userId = userId;
            this.trainId = trainId;
            this.pnrNumber = pnrNumber;
            this.totalAmount = totalAmount;
            this.passengerCount = passengerCount;
            this.journeyDate = journeyDate;
            this.timestamp = LocalDateTime.now();
        }

        // Getters and Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getTrainId() { return trainId; }
        public void setTrainId(Long trainId) { this.trainId = trainId; }
        
        public String getPnrNumber() { return pnrNumber; }
        public void setPnrNumber(String pnrNumber) { this.pnrNumber = pnrNumber; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public Integer getPassengerCount() { return passengerCount; }
        public void setPassengerCount(Integer passengerCount) { this.passengerCount = passengerCount; }
        
        public LocalDateTime getJourneyDate() { return journeyDate; }
        public void setJourneyDate(LocalDateTime journeyDate) { this.journeyDate = journeyDate; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }

    public static class BookingConfirmedEvent {
        @JsonProperty("eventId")
        private String eventId;
        
        @JsonProperty("bookingId")
        private Long bookingId;
        
        @JsonProperty("userId")
        private Long userId;
        
        @JsonProperty("pnrNumber")
        private String pnrNumber;
        
        @JsonProperty("confirmationTime")
        private LocalDateTime confirmationTime;
        
        @JsonProperty("eventType")
        private String eventType = "BOOKING_CONFIRMED";

        // Constructors
        public BookingConfirmedEvent() {}

        public BookingConfirmedEvent(Long bookingId, Long userId, String pnrNumber) {
            this.eventId = java.util.UUID.randomUUID().toString();
            this.bookingId = bookingId;
            this.userId = userId;
            this.pnrNumber = pnrNumber;
            this.confirmationTime = LocalDateTime.now();
        }

        // Getters and Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getPnrNumber() { return pnrNumber; }
        public void setPnrNumber(String pnrNumber) { this.pnrNumber = pnrNumber; }
        
        public LocalDateTime getConfirmationTime() { return confirmationTime; }
        public void setConfirmationTime(LocalDateTime confirmationTime) { this.confirmationTime = confirmationTime; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }

    public static class TicketConfirmationEvent {
        @JsonProperty("eventId")
        private String eventId;
        
        @JsonProperty("requestId")
        private String requestId;
        
        @JsonProperty("userId")
        private Long userId;
        
        @JsonProperty("trainId")
        private Long trainId;
        
        @JsonProperty("trainNumber")
        private String trainNumber;
        
        @JsonProperty("trainName")
        private String trainName;
        
        @JsonProperty("journeyDate")
        private LocalDateTime journeyDate;
        
        @JsonProperty("pnrNumber")
        private String pnrNumber;
        
        @JsonProperty("seatNumber")
        private String seatNumber;
        
        @JsonProperty("coachNumber")
        private String coachNumber;
        
        @JsonProperty("coachType")
        private String coachType;
        
        @JsonProperty("fare")
        private BigDecimal fare;
        
        @JsonProperty("confirmationTime")
        private LocalDateTime confirmationTime;
        
        @JsonProperty("previousStatus")
        private String previousStatus; // RAC, WAITLIST
        
        @JsonProperty("racNumber")
        private Integer racNumber;
        
        @JsonProperty("waitlistNumber")
        private Integer waitlistNumber;
        
        @JsonProperty("sourceStation")
        private String sourceStation;
        
        @JsonProperty("destinationStation")
        private String destinationStation;
        
        @JsonProperty("departureTime")
        private String departureTime;
        
        @JsonProperty("arrivalTime")
        private String arrivalTime;
        
        @JsonProperty("passengerName")
        private String passengerName;
        
        @JsonProperty("passengerEmail")
        private String passengerEmail;
        
        @JsonProperty("passengerPhone")
        private String passengerPhone;
        
        @JsonProperty("eventType")
        private String eventType = "TICKET_CONFIRMATION";

        // Constructors
        public TicketConfirmationEvent() {}

        public TicketConfirmationEvent(String requestId, Long userId, Long trainId, String trainNumber, 
                                     String trainName, LocalDateTime journeyDate, String pnrNumber,
                                     String seatNumber, String coachNumber, String coachType, 
                                     BigDecimal fare, String previousStatus) {
            this.eventId = java.util.UUID.randomUUID().toString();
            this.requestId = requestId;
            this.userId = userId;
            this.trainId = trainId;
            this.trainNumber = trainNumber;
            this.trainName = trainName;
            this.journeyDate = journeyDate;
            this.pnrNumber = pnrNumber;
            this.seatNumber = seatNumber;
            this.coachNumber = coachNumber;
            this.coachType = coachType;
            this.fare = fare;
            this.confirmationTime = LocalDateTime.now();
            this.previousStatus = previousStatus;
        }

        // Getters and Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getTrainId() { return trainId; }
        public void setTrainId(Long trainId) { this.trainId = trainId; }
        
        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
        
        public String getTrainName() { return trainName; }
        public void setTrainName(String trainName) { this.trainName = trainName; }
        
        public LocalDateTime getJourneyDate() { return journeyDate; }
        public void setJourneyDate(LocalDateTime journeyDate) { this.journeyDate = journeyDate; }
        
        public String getPnrNumber() { return pnrNumber; }
        public void setPnrNumber(String pnrNumber) { this.pnrNumber = pnrNumber; }
        
        public String getSeatNumber() { return seatNumber; }
        public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
        
        public String getCoachNumber() { return coachNumber; }
        public void setCoachNumber(String coachNumber) { this.coachNumber = coachNumber; }
        
        public String getCoachType() { return coachType; }
        public void setCoachType(String coachType) { this.coachType = coachType; }
        
        public BigDecimal getFare() { return fare; }
        public void setFare(BigDecimal fare) { this.fare = fare; }
        
        public LocalDateTime getConfirmationTime() { return confirmationTime; }
        public void setConfirmationTime(LocalDateTime confirmationTime) { this.confirmationTime = confirmationTime; }
        
        public String getPreviousStatus() { return previousStatus; }
        public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
        
        public Integer getRacNumber() { return racNumber; }
        public void setRacNumber(Integer racNumber) { this.racNumber = racNumber; }
        
        public Integer getWaitlistNumber() { return waitlistNumber; }
        public void setWaitlistNumber(Integer waitlistNumber) { this.waitlistNumber = waitlistNumber; }
        
        public String getSourceStation() { return sourceStation; }
        public void setSourceStation(String sourceStation) { this.sourceStation = sourceStation; }
        
        public String getDestinationStation() { return destinationStation; }
        public void setDestinationStation(String destinationStation) { this.destinationStation = destinationStation; }
        
        public String getDepartureTime() { return departureTime; }
        public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
        
        public String getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
        
        public String getPassengerName() { return passengerName; }
        public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
        
        public String getPassengerEmail() { return passengerEmail; }
        public void setPassengerEmail(String passengerEmail) { this.passengerEmail = passengerEmail; }
        
        public String getPassengerPhone() { return passengerPhone; }
        public void setPassengerPhone(String passengerPhone) { this.passengerPhone = passengerPhone; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }

    public static class BookingCancelledEvent {
        @JsonProperty("eventId")
        private String eventId;
        
        @JsonProperty("bookingId")
        private Long bookingId;
        
        @JsonProperty("userId")
        private Long userId;
        
        @JsonProperty("pnrNumber")
        private String pnrNumber;
        
        @JsonProperty("cancellationReason")
        private String cancellationReason;
        
        @JsonProperty("refundAmount")
        private BigDecimal refundAmount;
        
        @JsonProperty("cancellationTime")
        private LocalDateTime cancellationTime;
        
        @JsonProperty("eventType")
        private String eventType = "BOOKING_CANCELLED";

        // Constructors
        public BookingCancelledEvent() {}

        public BookingCancelledEvent(Long bookingId, Long userId, String pnrNumber, 
                                   String cancellationReason, BigDecimal refundAmount) {
            this.eventId = java.util.UUID.randomUUID().toString();
            this.bookingId = bookingId;
            this.userId = userId;
            this.pnrNumber = pnrNumber;
            this.cancellationReason = cancellationReason;
            this.refundAmount = refundAmount;
            this.cancellationTime = LocalDateTime.now();
        }

        // Getters and Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getPnrNumber() { return pnrNumber; }
        public void setPnrNumber(String pnrNumber) { this.pnrNumber = pnrNumber; }
        
        public String getCancellationReason() { return cancellationReason; }
        public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
        
        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
        
        public LocalDateTime getCancellationTime() { return cancellationTime; }
        public void setCancellationTime(LocalDateTime cancellationTime) { this.cancellationTime = cancellationTime; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }
}
