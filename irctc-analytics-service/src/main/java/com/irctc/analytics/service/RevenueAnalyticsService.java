package com.irctc.analytics.service;

import com.irctc.analytics.client.BookingServiceClient;
import com.irctc.analytics.client.PaymentServiceClient;
import com.irctc.analytics.client.UserServiceClient;
import com.irctc.analytics.dto.AnalyticsResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Revenue Analytics
 */
@Service
public class RevenueAnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(RevenueAnalyticsService.class);
    
    @Autowired
    private BookingServiceClient bookingServiceClient;
    
    @Autowired
    private PaymentServiceClient paymentServiceClient;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @CircuitBreaker(name = "booking-service", fallbackMethod = "getRevenueTrendsFallback")
    @Cacheable(value = "revenue-trends", key = "#period + '-' + #startDate + '-' + #endDate")
    public AnalyticsResponse.RevenueTrends getRevenueTrends(String period, LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching revenue trends for period: {}, from {} to {}", period, startDate, endDate);
        
        // Get bookings in date range
        List<BookingServiceClient.BookingDTO> bookings = bookingServiceClient.getBookingsByDateRange(startDate, endDate);
        
        // Get payments
        Map<String, Object> paymentOverview = paymentServiceClient.getPaymentOverview(startDate, endDate);
        
        // Group by period
        Map<LocalDate, List<BookingServiceClient.BookingDTO>> groupedBookings = groupBookingsByPeriod(bookings, period);
        
        List<AnalyticsResponse.RevenueDataPoint> dataPoints = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        for (Map.Entry<LocalDate, List<BookingServiceClient.BookingDTO>> entry : groupedBookings.entrySet()) {
            LocalDate date = entry.getKey();
            List<BookingServiceClient.BookingDTO> dayBookings = entry.getValue();
            
            BigDecimal dayRevenue = dayBookings.stream()
                    .filter(b -> b.getTotalAmount() != null && "CONFIRMED".equals(b.getBookingStatus()))
                    .map(b -> BigDecimal.valueOf(b.getTotalAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            totalRevenue = totalRevenue.add(dayRevenue);
            
            BigDecimal avgBookingValue = dayBookings.isEmpty() ? BigDecimal.ZERO :
                    dayRevenue.divide(BigDecimal.valueOf(dayBookings.size()), 2, RoundingMode.HALF_UP);
            
            dataPoints.add(new AnalyticsResponse.RevenueDataPoint(
                    date,
                    dayRevenue,
                    dayBookings.size(),
                    avgBookingValue
            ));
        }
        
        // Calculate growth rate
        BigDecimal previousPeriodRevenue = calculatePreviousPeriodRevenue(bookings, period, startDate, endDate);
        BigDecimal growthRate = previousPeriodRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                totalRevenue.subtract(previousPeriodRevenue)
                        .divide(previousPeriodRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
        
        BigDecimal averageRevenue = dataPoints.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(dataPoints.size()), 2, RoundingMode.HALF_UP);
        
        return new AnalyticsResponse.RevenueTrends(
                period,
                dataPoints,
                totalRevenue,
                averageRevenue,
                growthRate,
                previousPeriodRevenue
        );
    }
    
    @CircuitBreaker(name = "booking-service", fallbackMethod = "getBookingTrendsFallback")
    @Cacheable(value = "booking-trends", key = "#startDate + '-' + #endDate")
    public AnalyticsResponse.BookingTrends getBookingTrends(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching booking trends from {} to {}", startDate, endDate);
        
        List<BookingServiceClient.BookingDTO> bookings = bookingServiceClient.getBookingsByDateRange(startDate, endDate);
        
        // Group by date
        Map<LocalDate, List<BookingServiceClient.BookingDTO>> bookingsByDate = bookings.stream()
                .collect(Collectors.groupingBy(b -> 
                        b.getBookingDate() != null ? b.getBookingDate() : LocalDate.now()));
        
        List<AnalyticsResponse.BookingDataPoint> dataPoints = new ArrayList<>();
        int totalBookings = bookings.size();
        int confirmedBookings = 0;
        int cancelledBookings = 0;
        int waitlistBookings = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;
        
        for (Map.Entry<LocalDate, List<BookingServiceClient.BookingDTO>> entry : bookingsByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<BookingServiceClient.BookingDTO> dayBookings = entry.getValue();
            
            int confirmed = (int) dayBookings.stream()
                    .filter(b -> "CONFIRMED".equals(b.getBookingStatus()))
                    .count();
            int cancelled = (int) dayBookings.stream()
                    .filter(b -> b.getIsCancelled() != null && b.getIsCancelled())
                    .count();
            int waitlist = (int) dayBookings.stream()
                    .filter(b -> "WAITLIST".equals(b.getBookingStatus()))
                    .count();
            
            confirmedBookings += confirmed;
            cancelledBookings += cancelled;
            waitlistBookings += waitlist;
            
            BigDecimal dayRevenue = dayBookings.stream()
                    .filter(b -> b.getTotalAmount() != null && "CONFIRMED".equals(b.getBookingStatus()))
                    .map(b -> BigDecimal.valueOf(b.getTotalAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            totalRevenue = totalRevenue.add(dayRevenue);
            
            dataPoints.add(new AnalyticsResponse.BookingDataPoint(
                    date,
                    dayBookings.size(),
                    confirmed,
                    cancelled,
                    waitlist,
                    dayRevenue
            ));
        }
        
        // Get refund statistics
        Map<String, Object> refundStats = paymentServiceClient.getRefundStatistics(startDate, endDate);
        if (refundStats.containsKey("totalRefundAmount")) {
            refundAmount = BigDecimal.valueOf((Double) refundStats.get("totalRefundAmount"));
        }
        
        double cancellationRate = totalBookings > 0 ? (double) cancelledBookings / totalBookings * 100 : 0.0;
        double confirmationRate = totalBookings > 0 ? (double) confirmedBookings / totalBookings * 100 : 0.0;
        double avgBookingValue = totalBookings > 0 ? totalRevenue.doubleValue() / totalBookings : 0.0;
        
        return new AnalyticsResponse.BookingTrends(
                dataPoints,
                totalBookings,
                confirmedBookings,
                cancelledBookings,
                waitlistBookings,
                cancellationRate,
                confirmationRate,
                totalRevenue,
                refundAmount,
                avgBookingValue
        );
    }
    
    @CircuitBreaker(name = "booking-service", fallbackMethod = "getRoutePerformanceFallback")
    @Cacheable(value = "route-performance", key = "#startDate + '-' + #endDate")
    public AnalyticsResponse.RoutePerformance getRoutePerformance(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching route performance from {} to {}", startDate, endDate);
        
        List<BookingServiceClient.BookingDTO> bookings = bookingServiceClient.getBookingsByDateRange(startDate, endDate);
        
        // Group by route (source-destination)
        Map<String, List<BookingServiceClient.BookingDTO>> bookingsByRoute = bookings.stream()
                .filter(b -> b.getSourceStation() != null && b.getDestinationStation() != null)
                .collect(Collectors.groupingBy(b -> 
                        b.getSourceStation() + "-" + b.getDestinationStation()));
        
        List<AnalyticsResponse.RoutePerformanceData> routeData = new ArrayList<>();
        
        for (Map.Entry<String, List<BookingServiceClient.BookingDTO>> entry : bookingsByRoute.entrySet()) {
            String routeKey = entry.getKey();
            List<BookingServiceClient.BookingDTO> routeBookings = entry.getValue();
            
            String[] stations = routeKey.split("-");
            String source = stations[0];
            String destination = stations.length > 1 ? stations[1] : "";
            
            int bookingCount = routeBookings.size();
            BigDecimal totalRevenue = routeBookings.stream()
                    .filter(b -> b.getTotalAmount() != null && "CONFIRMED".equals(b.getBookingStatus()))
                    .map(b -> BigDecimal.valueOf(b.getTotalAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal avgRevenue = bookingCount > 0 ?
                    totalRevenue.divide(BigDecimal.valueOf(bookingCount), 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;
            
            // Calculate occupancy rate (simplified - would need train capacity data)
            double occupancyRate = bookingCount > 0 ? Math.min(100.0, (bookingCount / 10.0) * 100) : 0.0;
            
            routeData.add(new AnalyticsResponse.RoutePerformanceData(
                    routeKey,
                    source,
                    destination,
                    bookingCount,
                    totalRevenue,
                    avgRevenue,
                    occupancyRate,
                    0, // popularity rank will be set later
                    0.0 // distance would come from train service
            ));
        }
        
        // Sort by revenue and set popularity rank
        routeData.sort((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()));
        for (int i = 0; i < routeData.size(); i++) {
            routeData.get(i).setPopularityRank(i + 1);
        }
        
        AnalyticsResponse.RoutePerformanceData topRoute = routeData.isEmpty() ? null : routeData.get(0);
        AnalyticsResponse.RoutePerformanceData leastProfitableRoute = routeData.isEmpty() ? null : 
                routeData.get(routeData.size() - 1);
        
        return new AnalyticsResponse.RoutePerformance(
                routeData,
                routeData.size(),
                topRoute,
                leastProfitableRoute
        );
    }
    
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserSegmentationFallback")
    @Cacheable(value = "user-segmentation")
    public AnalyticsResponse.UserSegmentation getUserSegmentation() {
        logger.info("Fetching user segmentation");
        
        List<BookingServiceClient.BookingDTO> allBookings = bookingServiceClient.getAllBookings();
        List<UserServiceClient.UserDTO> allUsers = userServiceClient.getAllUsers();
        
        // Group bookings by user
        Map<Long, List<BookingServiceClient.BookingDTO>> bookingsByUser = allBookings.stream()
                .collect(Collectors.groupingBy(BookingServiceClient.BookingDTO::getUserId));
        
        // Calculate user metrics
        Map<Long, UserMetrics> userMetrics = new HashMap<>();
        for (Map.Entry<Long, List<BookingServiceClient.BookingDTO>> entry : bookingsByUser.entrySet()) {
            Long userId = entry.getKey();
            List<BookingServiceClient.BookingDTO> userBookings = entry.getValue();
            
            BigDecimal totalRevenue = userBookings.stream()
                    .filter(b -> b.getTotalAmount() != null && "CONFIRMED".equals(b.getBookingStatus()))
                    .map(b -> BigDecimal.valueOf(b.getTotalAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            userMetrics.put(userId, new UserMetrics(
                    userBookings.size(),
                    totalRevenue,
                    totalRevenue.divide(BigDecimal.valueOf(Math.max(1, userBookings.size())), 2, RoundingMode.HALF_UP)
            ));
        }
        
        // Segment users
        List<AnalyticsResponse.UserSegment> segments = new ArrayList<>();
        int vipCount = 0;
        int regularCount = 0;
        int newCount = 0;
        int inactiveCount = 0;
        BigDecimal vipRevenue = BigDecimal.ZERO;
        BigDecimal regularRevenue = BigDecimal.ZERO;
        BigDecimal newRevenue = BigDecimal.ZERO;
        
        for (UserServiceClient.UserDTO user : allUsers) {
            UserMetrics metrics = userMetrics.getOrDefault(user.getId(), 
                    new UserMetrics(0, BigDecimal.ZERO, BigDecimal.ZERO));
            
            if (metrics.bookingCount >= 20 && metrics.totalRevenue.compareTo(BigDecimal.valueOf(50000)) >= 0) {
                vipCount++;
                vipRevenue = vipRevenue.add(metrics.totalRevenue);
            } else if (metrics.bookingCount >= 5) {
                regularCount++;
                regularRevenue = regularRevenue.add(metrics.totalRevenue);
            } else if (metrics.bookingCount > 0) {
                newCount++;
                newRevenue = newRevenue.add(metrics.totalRevenue);
            } else {
                inactiveCount++;
            }
        }
        
        int totalUsers = allUsers.size();
        BigDecimal totalRevenue = vipRevenue.add(regularRevenue).add(newRevenue);
        
        if (vipCount > 0) {
            segments.add(createSegment("VIP", vipCount, vipRevenue, userMetrics, totalUsers, totalRevenue));
        }
        if (regularCount > 0) {
            segments.add(createSegment("Regular", regularCount, regularRevenue, userMetrics, totalUsers, totalRevenue));
        }
        if (newCount > 0) {
            segments.add(createSegment("New", newCount, newRevenue, userMetrics, totalUsers, totalRevenue));
        }
        if (inactiveCount > 0) {
            segments.add(new AnalyticsResponse.UserSegment(
                    "Inactive",
                    inactiveCount,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0,
                    (double) inactiveCount / totalUsers * 100
            ));
        }
        
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("VIP", vipCount);
        distribution.put("Regular", regularCount);
        distribution.put("New", newCount);
        distribution.put("Inactive", inactiveCount);
        
        return new AnalyticsResponse.UserSegmentation(segments, totalUsers, distribution);
    }
    
    @CircuitBreaker(name = "booking-service", fallbackMethod = "getForecastFallback")
    @Cacheable(value = "forecast", key = "#forecastType + '-' + #days")
    public AnalyticsResponse.Forecast getForecast(String forecastType, int days) {
        logger.info("Generating forecast for type: {}, days: {}", forecastType, days);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(90); // Use last 90 days for forecasting
        
        List<BookingServiceClient.BookingDTO> historicalBookings = 
                bookingServiceClient.getBookingsByDateRange(startDate, endDate);
        
        // Simple linear regression for forecasting
        List<AnalyticsResponse.ForecastDataPoint> forecastData = new ArrayList<>();
        
        if ("revenue".equals(forecastType)) {
            // Calculate average daily revenue
            Map<LocalDate, BigDecimal> dailyRevenue = historicalBookings.stream()
                    .filter(b -> b.getTotalAmount() != null && "CONFIRMED".equals(b.getBookingStatus()))
                    .collect(Collectors.groupingBy(
                            b -> b.getBookingDate() != null ? b.getBookingDate() : LocalDate.now(),
                            Collectors.reducing(BigDecimal.ZERO,
                                    b -> BigDecimal.valueOf(b.getTotalAmount()),
                                    BigDecimal::add)));
            
            BigDecimal avgDailyRevenue = dailyRevenue.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(Math.max(1, dailyRevenue.size())), 2, RoundingMode.HALF_UP);
            
            for (int i = 1; i <= days; i++) {
                LocalDate forecastDate = endDate.plusDays(i);
                BigDecimal predicted = avgDailyRevenue;
                BigDecimal lowerBound = predicted.multiply(BigDecimal.valueOf(0.8));
                BigDecimal upperBound = predicted.multiply(BigDecimal.valueOf(1.2));
                
                forecastData.add(new AnalyticsResponse.ForecastDataPoint(
                        forecastDate,
                        predicted,
                        lowerBound,
                        upperBound,
                        0.75 // 75% confidence
                ));
            }
        } else if ("bookings".equals(forecastType)) {
            // Calculate average daily bookings
            Map<LocalDate, Long> dailyBookings = historicalBookings.stream()
                    .collect(Collectors.groupingBy(
                            b -> b.getBookingDate() != null ? b.getBookingDate() : LocalDate.now(),
                            Collectors.counting()));
            
            double avgDailyBookings = dailyBookings.values().stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            
            for (int i = 1; i <= days; i++) {
                LocalDate forecastDate = endDate.plusDays(i);
                BigDecimal predicted = BigDecimal.valueOf(avgDailyBookings);
                BigDecimal lowerBound = predicted.multiply(BigDecimal.valueOf(0.8));
                BigDecimal upperBound = predicted.multiply(BigDecimal.valueOf(1.2));
                
                forecastData.add(new AnalyticsResponse.ForecastDataPoint(
                        forecastDate,
                        predicted,
                        lowerBound,
                        upperBound,
                        0.75
                ));
            }
        }
        
        BigDecimal predictedTotal = forecastData.stream()
                .map(AnalyticsResponse.ForecastDataPoint::getPredictedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new AnalyticsResponse.Forecast(
                forecastType,
                forecastData,
                predictedTotal,
                forecastData.size(),
                0.75,
                "linear"
        );
    }
    
    // Helper methods
    private Map<LocalDate, List<BookingServiceClient.BookingDTO>> groupBookingsByPeriod(
            List<BookingServiceClient.BookingDTO> bookings, String period) {
        if ("daily".equals(period)) {
            return bookings.stream()
                    .collect(Collectors.groupingBy(b -> 
                            b.getBookingDate() != null ? b.getBookingDate() : LocalDate.now()));
        } else if ("weekly".equals(period)) {
            return bookings.stream()
                    .collect(Collectors.groupingBy(b -> {
                        LocalDate date = b.getBookingDate() != null ? b.getBookingDate() : LocalDate.now();
                        return date.with(java.time.DayOfWeek.MONDAY);
                    }));
        } else { // monthly
            return bookings.stream()
                    .collect(Collectors.groupingBy(b -> {
                        LocalDate date = b.getBookingDate() != null ? b.getBookingDate() : LocalDate.now();
                        return date.withDayOfMonth(1);
                    }));
        }
    }
    
    private BigDecimal calculatePreviousPeriodRevenue(
            List<BookingServiceClient.BookingDTO> currentBookings, 
            String period, LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate prevStartDate = startDate.minusDays(days);
        LocalDate prevEndDate = startDate;
        
        List<BookingServiceClient.BookingDTO> prevBookings = 
                bookingServiceClient.getBookingsByDateRange(prevStartDate, prevEndDate);
        
        return prevBookings.stream()
                .filter(b -> b.getTotalAmount() != null && "CONFIRMED".equals(b.getBookingStatus()))
                .map(b -> BigDecimal.valueOf(b.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private AnalyticsResponse.UserSegment createSegment(
            String name, int count, BigDecimal revenue, 
            Map<Long, UserMetrics> userMetrics, int totalUsers, BigDecimal totalRevenue) {
        double avgBookings = userMetrics.values().stream()
                .filter(m -> m.bookingCount > 0)
                .mapToInt(m -> m.bookingCount)
                .average()
                .orElse(0.0);
        
        BigDecimal avgRevenue = count > 0 ? 
                revenue.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        
        return new AnalyticsResponse.UserSegment(
                name,
                count,
                revenue,
                avgRevenue,
                (int) avgBookings,
                (double) count / totalUsers * 100
        );
    }
    
    // Fallback methods
    public AnalyticsResponse.RevenueTrends getRevenueTrendsFallback(String period, LocalDate startDate, LocalDate endDate, Exception e) {
        logger.warn("Fallback: Returning empty revenue trends", e);
        return new AnalyticsResponse.RevenueTrends(period, new ArrayList<>(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
    
    public AnalyticsResponse.BookingTrends getBookingTrendsFallback(LocalDate startDate, LocalDate endDate, Exception e) {
        logger.warn("Fallback: Returning empty booking trends", e);
        return new AnalyticsResponse.BookingTrends(new ArrayList<>(), 0, 0, 0, 0, 0.0, 0.0, BigDecimal.ZERO, BigDecimal.ZERO, 0.0);
    }
    
    public AnalyticsResponse.RoutePerformance getRoutePerformanceFallback(LocalDate startDate, LocalDate endDate, Exception e) {
        logger.warn("Fallback: Returning empty route performance", e);
        return new AnalyticsResponse.RoutePerformance(new ArrayList<>(), 0, null, null);
    }
    
    public AnalyticsResponse.UserSegmentation getUserSegmentationFallback(Exception e) {
        logger.warn("Fallback: Returning empty user segmentation", e);
        return new AnalyticsResponse.UserSegmentation(new ArrayList<>(), 0, new HashMap<>());
    }
    
    public AnalyticsResponse.Forecast getForecastFallback(String forecastType, int days, Exception e) {
        logger.warn("Fallback: Returning empty forecast", e);
        return new AnalyticsResponse.Forecast(forecastType, new ArrayList<>(), BigDecimal.ZERO, 0, 0.0, "linear");
    }
    
    // Inner class for user metrics
    private static class UserMetrics {
        int bookingCount;
        BigDecimal totalRevenue;
        BigDecimal averageRevenue;
        
        UserMetrics(int bookingCount, BigDecimal totalRevenue, BigDecimal averageRevenue) {
            this.bookingCount = bookingCount;
            this.totalRevenue = totalRevenue;
            this.averageRevenue = averageRevenue;
        }
    }
}

