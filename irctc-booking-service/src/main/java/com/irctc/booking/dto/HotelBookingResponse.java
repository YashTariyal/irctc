package com.irctc.booking.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for hotel booking response
 */
@Data
public class HotelBookingResponse {
    private Long id;
    private Long userId;
    private Long hotelId;
    private Long trainBookingId;
    private String bookingReference;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfRooms;
    private Integer numberOfGuests;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String status;
    private String paymentStatus;
    private Boolean isPackageDeal;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
}

