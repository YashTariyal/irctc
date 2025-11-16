package com.irctc.booking.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for hotel booking request
 */
@Data
public class HotelBookingRequest {
    private Long userId;
    private Long hotelId;
    private Long trainBookingId; // Optional: for package deals
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfRooms;
    private Integer numberOfGuests;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private String specialRequests;
    private Boolean isPackageDeal = false;
    private BigDecimal discountAmount; // Discount for package deals
}

