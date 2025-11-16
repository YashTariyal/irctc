package com.irctc.booking.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for hotel package deals (Train + Hotel)
 */
@Data
public class HotelPackageResponse {
    private String route; // Train route
    private String originStation;
    private String destinationStation;
    private List<HotelPackage> packages;
    
    @Data
    public static class HotelPackage {
        private Long hotelId;
        private String hotelName;
        private String location;
        private BigDecimal hotelPricePerNight;
        private BigDecimal trainFare;
        private BigDecimal packagePrice; // Combined price
        private BigDecimal discountAmount; // Discount for package
        private BigDecimal finalPrice; // Final price after discount
        private BigDecimal savings; // Amount saved with package
        private Integer nights;
        private String description;
    }
}

