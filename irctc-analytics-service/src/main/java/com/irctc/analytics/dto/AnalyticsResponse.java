package com.irctc.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTOs for Analytics API responses
 */
public class AnalyticsResponse {
    
    /**
     * Revenue Trends Response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueTrends {
        private String period; // daily, weekly, monthly
        private List<RevenueDataPoint> dataPoints;
        private BigDecimal totalRevenue;
        private BigDecimal averageRevenue;
        private BigDecimal growthRate;
        private BigDecimal previousPeriodRevenue;
    }
    
    /**
     * Revenue Data Point
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueDataPoint {
        private LocalDate date;
        private BigDecimal revenue;
        private Integer bookingCount;
        private BigDecimal averageBookingValue;
    }
    
    /**
     * Booking Trends Response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingTrends {
        private List<BookingDataPoint> dataPoints;
        private Integer totalBookings;
        private Integer confirmedBookings;
        private Integer cancelledBookings;
        private Integer waitlistBookings;
        private Double cancellationRate;
        private Double confirmationRate;
        private BigDecimal totalRevenue;
        private BigDecimal refundAmount;
        private Double averageBookingValue;
    }
    
    /**
     * Booking Data Point
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingDataPoint {
        private LocalDate date;
        private Integer bookingCount;
        private Integer confirmedCount;
        private Integer cancelledCount;
        private Integer waitlistCount;
        private BigDecimal revenue;
    }
    
    /**
     * Route Performance Response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoutePerformance {
        private List<RoutePerformanceData> routes;
        private Integer totalRoutes;
        private RoutePerformanceData topRoute;
        private RoutePerformanceData leastProfitableRoute;
    }
    
    /**
     * Route Performance Data
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoutePerformanceData {
        private String routeCode;
        private String sourceStation;
        private String destinationStation;
        private Integer bookingCount;
        private BigDecimal totalRevenue;
        private BigDecimal averageRevenue;
        private Double occupancyRate;
        private Integer popularityRank;
        private Double distance;
    }
    
    /**
     * User Segmentation Response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSegmentation {
        private List<UserSegment> segments;
        private Integer totalUsers;
        private Map<String, Integer> segmentDistribution;
    }
    
    /**
     * User Segment
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSegment {
        private String segmentName; // VIP, Regular, New, Inactive
        private Integer userCount;
        private BigDecimal totalRevenue;
        private BigDecimal averageRevenue;
        private Integer averageBookings;
        private Double percentageOfTotal;
    }
    
    /**
     * Forecast Response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Forecast {
        private String forecastType; // revenue, bookings
        private List<ForecastDataPoint> forecastData;
        private BigDecimal predictedRevenue;
        private Integer predictedBookings;
        private Double confidenceLevel;
        private String forecastMethod; // linear, exponential, seasonal
    }
    
    /**
     * Forecast Data Point
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastDataPoint {
        private LocalDate date;
        private BigDecimal predictedValue;
        private BigDecimal lowerBound;
        private BigDecimal upperBound;
        private Double confidence;
    }
}

