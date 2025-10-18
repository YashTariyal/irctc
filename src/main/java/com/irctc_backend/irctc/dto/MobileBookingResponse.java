package com.irctc_backend.irctc.dto;

import com.irctc_backend.irctc.entity.Booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mobile-optimized booking response DTO
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class MobileBookingResponse {
    
    private Long id;
    private String pnrNumber;
    private String trainNumber;
    private String trainName;
    private String sourceStationCode;
    private String sourceStationName;
    private String destinationStationCode;
    private String destinationStationName;
    private LocalDate journeyDate;
    private LocalDateTime bookingDate;
    private String status;
    private String paymentStatus;
    private String quotaType;
    private Integer passengerCount;
    private BigDecimal totalAmount;
    private String coachNumber;
    private String seatNumbers;
    private String berthTypes;
    private Boolean isTatkal;
    private Boolean isCancelled;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private BigDecimal refundAmount;
    private String bookingClass;
    private String journeyType;
    private String boardingStation;
    private String reservationUpto;
    
    // Constructors
    public MobileBookingResponse() {}
    
    public MobileBookingResponse(Booking booking) {
        this.id = booking.getId();
        this.pnrNumber = booking.getPnrNumber();
        this.trainNumber = booking.getTrain() != null ? booking.getTrain().getTrainNumber() : null;
        this.trainName = booking.getTrain() != null ? booking.getTrain().getTrainName() : null;
        this.sourceStationCode = booking.getTrain() != null && booking.getTrain().getSourceStation() != null ? 
            booking.getTrain().getSourceStation().getStationCode() : null;
        this.sourceStationName = booking.getTrain() != null && booking.getTrain().getSourceStation() != null ? 
            booking.getTrain().getSourceStation().getStationName() : null;
        this.destinationStationCode = booking.getTrain() != null && booking.getTrain().getDestinationStation() != null ? 
            booking.getTrain().getDestinationStation().getStationCode() : null;
        this.destinationStationName = booking.getTrain() != null && booking.getTrain().getDestinationStation() != null ? 
            booking.getTrain().getDestinationStation().getStationName() : null;
        this.journeyDate = booking.getJourneyDate();
        this.bookingDate = booking.getBookingDate();
        this.status = booking.getStatus() != null ? booking.getStatus().name() : null;
        this.paymentStatus = booking.getPaymentStatus() != null ? booking.getPaymentStatus().name() : null;
        this.quotaType = booking.getQuotaType() != null ? booking.getQuotaType().name() : null;
        this.passengerCount = 1; // Simplified - in real implementation, you'd count passengers
        this.totalAmount = booking.getTotalFare();
        this.coachNumber = booking.getCoach() != null ? booking.getCoach().getCoachNumber() : null;
        this.seatNumbers = booking.getSeat() != null ? booking.getSeat().getSeatNumber() : null;
        this.berthTypes = booking.getSeat() != null ? booking.getSeat().getBerthType().name() : null;
        this.isTatkal = booking.getIsTatkal();
        this.isCancelled = booking.getIsCancelled();
        this.cancelledAt = booking.getCancellationDate();
        this.cancellationReason = "Cancelled by user"; // Simplified
        this.refundAmount = booking.getRefundAmount();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPnrNumber() {
        return pnrNumber;
    }
    
    public void setPnrNumber(String pnrNumber) {
        this.pnrNumber = pnrNumber;
    }
    
    public String getTrainNumber() {
        return trainNumber;
    }
    
    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }
    
    public String getTrainName() {
        return trainName;
    }
    
    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }
    
    public String getSourceStationCode() {
        return sourceStationCode;
    }
    
    public void setSourceStationCode(String sourceStationCode) {
        this.sourceStationCode = sourceStationCode;
    }
    
    public String getSourceStationName() {
        return sourceStationName;
    }
    
    public void setSourceStationName(String sourceStationName) {
        this.sourceStationName = sourceStationName;
    }
    
    public String getDestinationStationCode() {
        return destinationStationCode;
    }
    
    public void setDestinationStationCode(String destinationStationCode) {
        this.destinationStationCode = destinationStationCode;
    }
    
    public String getDestinationStationName() {
        return destinationStationName;
    }
    
    public void setDestinationStationName(String destinationStationName) {
        this.destinationStationName = destinationStationName;
    }
    
    public LocalDate getJourneyDate() {
        return journeyDate;
    }
    
    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }
    
    public LocalDateTime getBookingDate() {
        return bookingDate;
    }
    
    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getQuotaType() {
        return quotaType;
    }
    
    public void setQuotaType(String quotaType) {
        this.quotaType = quotaType;
    }
    
    public Integer getPassengerCount() {
        return passengerCount;
    }
    
    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getCoachNumber() {
        return coachNumber;
    }
    
    public void setCoachNumber(String coachNumber) {
        this.coachNumber = coachNumber;
    }
    
    public String getSeatNumbers() {
        return seatNumbers;
    }
    
    public void setSeatNumbers(String seatNumbers) {
        this.seatNumbers = seatNumbers;
    }
    
    public String getBerthTypes() {
        return berthTypes;
    }
    
    public void setBerthTypes(String berthTypes) {
        this.berthTypes = berthTypes;
    }
    
    public Boolean getIsTatkal() {
        return isTatkal;
    }
    
    public void setIsTatkal(Boolean isTatkal) {
        this.isTatkal = isTatkal;
    }
    
    public Boolean getIsCancelled() {
        return isCancelled;
    }
    
    public void setIsCancelled(Boolean isCancelled) {
        this.isCancelled = isCancelled;
    }
    
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    
    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
    
    public String getBookingClass() {
        return bookingClass;
    }
    
    public void setBookingClass(String bookingClass) {
        this.bookingClass = bookingClass;
    }
    
    public String getJourneyType() {
        return journeyType;
    }
    
    public void setJourneyType(String journeyType) {
        this.journeyType = journeyType;
    }
    
    public String getBoardingStation() {
        return boardingStation;
    }
    
    public void setBoardingStation(String boardingStation) {
        this.boardingStation = boardingStation;
    }
    
    public String getReservationUpto() {
        return reservationUpto;
    }
    
    public void setReservationUpto(String reservationUpto) {
        this.reservationUpto = reservationUpto;
    }
}
