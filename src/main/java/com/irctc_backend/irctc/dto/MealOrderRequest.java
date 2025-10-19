package com.irctc_backend.irctc.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.List;

public class MealOrderRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Train ID is required")
    private Long trainId;

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemRequest> orderItems;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Invalid station code format")
    private String deliveryStationCode;

    private String deliveryStationName;

    private LocalDateTime deliveryTime;

    private String specialInstructions;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number format")
    private String contactPhone;

    private String seatNumber;

    private String coachNumber;

    public static class OrderItemRequest {
        @NotNull(message = "Meal item ID is required")
        private Long mealItemId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        private String specialInstructions;

        // Getters and Setters
        public Long getMealItemId() { return mealItemId; }
        public void setMealItemId(Long mealItemId) { this.mealItemId = mealItemId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getSpecialInstructions() { return specialInstructions; }
        public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    }

    // Getters and Setters
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Long getTrainId() { return trainId; }
    public void setTrainId(Long trainId) { this.trainId = trainId; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public List<OrderItemRequest> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemRequest> orderItems) { this.orderItems = orderItems; }
    public String getDeliveryStationCode() { return deliveryStationCode; }
    public void setDeliveryStationCode(String deliveryStationCode) { this.deliveryStationCode = deliveryStationCode; }
    public String getDeliveryStationName() { return deliveryStationName; }
    public void setDeliveryStationName(String deliveryStationName) { this.deliveryStationName = deliveryStationName; }
    public LocalDateTime getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(LocalDateTime deliveryTime) { this.deliveryTime = deliveryTime; }
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getCoachNumber() { return coachNumber; }
    public void setCoachNumber(String coachNumber) { this.coachNumber = coachNumber; }
}
