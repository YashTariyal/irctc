package com.irctc_backend.irctc.dto;

import com.irctc_backend.irctc.entity.MealOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MealOrderResponse {

    private Long id;
    private String orderNumber;
    private Long bookingId;
    private String pnrNumber;
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private Long vendorId;
    private String vendorName;
    private String stationCode;
    private BigDecimal totalAmount;
    private MealOrder.OrderStatus orderStatus;
    private MealOrder.PaymentStatus paymentStatus;
    private String deliveryStationCode;
    private String deliveryStationName;
    private LocalDateTime deliveryTime;
    private String specialInstructions;
    private String contactPhone;
    private String seatNumber;
    private String coachNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> orderItems;

    public static class OrderItemResponse {
        private Long id;
        private Long mealItemId;
        private String itemName;
        private String description;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String specialInstructions;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getMealItemId() { return mealItemId; }
        public void setMealItemId(Long mealItemId) { this.mealItemId = mealItemId; }
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        public String getSpecialInstructions() { return specialInstructions; }
        public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getPnrNumber() { return pnrNumber; }
    public void setPnrNumber(String pnrNumber) { this.pnrNumber = pnrNumber; }
    public Long getTrainId() { return trainId; }
    public void setTrainId(Long trainId) { this.trainId = trainId; }
    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public MealOrder.OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(MealOrder.OrderStatus orderStatus) { this.orderStatus = orderStatus; }
    public MealOrder.PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(MealOrder.PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<OrderItemResponse> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemResponse> orderItems) { this.orderItems = orderItems; }
}
