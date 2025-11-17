package com.irctc.booking.dto.analytics;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Data
public class TravelAnalyticsResponse {
    private Long userId;
    private Integer totalBookings;
    private Integer confirmedTrips;
    private Integer cancelledTrips;
    private BigDecimal totalSpent = BigDecimal.ZERO;
    private BigDecimal averageFare = BigDecimal.ZERO;
    private Double totalDistanceKm = 0.0;
    private Integer uniqueRoutes = 0;
    private Integer uniqueTrains = 0;
    private List<MonthlySummary> monthlySummaries = Collections.emptyList();
    private List<RouteSummary> favoriteRoutes = Collections.emptyList();
    private List<TrainSummary> topTrains = Collections.emptyList();

    @Data
    public static class MonthlySummary {
        private String month;
        private Integer trips;
        private BigDecimal amount;
    }

    @Data
    public static class RouteSummary {
        private String sourceStation;
        private String destinationStation;
        private Long trips;
        private BigDecimal totalSpent;
        private Double distanceKm;
    }

    @Data
    public static class TrainSummary {
        private Long trainId;
        private String trainName;
        private Long trips;
        private BigDecimal totalSpent;
    }
}

