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
