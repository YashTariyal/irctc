package com.irctc.booking.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for hotel search request
 */
@Data
public class HotelSearchRequest {
    private String location; // City or area name
    private String stationCode; // Nearest railway station code
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfRooms = 1;
    private Integer numberOfGuests = 1;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minRating; // Minimum rating (1.0 to 5.0)
    private String amenities; // Comma-separated amenities filter
}

