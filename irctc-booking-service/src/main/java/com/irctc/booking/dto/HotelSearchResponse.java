package com.irctc.booking.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for hotel search response
 */
@Data
public class HotelSearchResponse {
    private Long id;
    private String name;
    private String location;
    private String nearestStationCode;
    private String address;
    private String city;
    private String state;
    private BigDecimal rating;
    private BigDecimal pricePerNight;
    private Integer availableRooms;
    private String amenities;
    private String description;
    private String imageUrl;
    private BigDecimal totalPrice; // Total price for the stay duration
    private Integer nights; // Number of nights
}

