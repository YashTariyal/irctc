package com.irctc.booking.service;

import com.irctc.booking.client.TrainServiceClient;
import com.irctc.booking.dto.analytics.TravelAnalyticsResponse;
import com.irctc.booking.dto.analytics.TravelExportResponse;
import com.irctc.booking.dto.analytics.TravelTimelineEntry;
import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.repository.SimpleBookingRepository;
import com.irctc.booking.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.*;
import java.util.Base64.Encoder;
import java.util.stream.Collectors;

@Service
public class TravelAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(TravelAnalyticsService.class);
    private static final int MAX_TOP_ROUTES = 5;
    private static final int MAX_TOP_TRAINS = 5;
    private static final Encoder BASE64 = Base64.getEncoder();

    private final SimpleBookingRepository bookingRepository;
    private final TrainServiceClient trainServiceClient;

    public TravelAnalyticsService(SimpleBookingRepository bookingRepository,
                                  TrainServiceClient trainServiceClient) {
        this.bookingRepository = bookingRepository;
        this.trainServiceClient = trainServiceClient;
    }

    public TravelAnalyticsResponse getTravelAnalytics(Long userId) {
        List<SimpleBooking> bookings = loadUserBookings(userId);
        TravelAnalyticsResponse response = new TravelAnalyticsResponse();
        response.setUserId(userId);
        response.setTotalBookings(bookings.size());

        if (bookings.isEmpty()) {
            return response;
        }

        Map<Long, TrainServiceClient.TrainResponse> trainCache = new HashMap<>();
        Map<String, RouteAccumulator> routeAccumulators = new HashMap<>();
        Map<Long, TrainAccumulator> trainAccumulators = new HashMap<>();
        Map<String, MonthlyAccumulator> monthlyAccumulators = new HashMap<>();

        BigDecimal totalSpent = BigDecimal.ZERO;
        double totalDistance = 0d;
        int confirmedTrips = 0;
        int cancelledTrips = 0;

        for (SimpleBooking booking : bookings) {
            if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
                confirmedTrips++;
            } else if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
                cancelledTrips++;
            }

            totalSpent = totalSpent.add(
                booking.getTotalFare() != null ? booking.getTotalFare() : BigDecimal.ZERO
            );

            YearMonth yearMonth = booking.getBookingTime() != null
                ? YearMonth.from(booking.getBookingTime())
                : YearMonth.now();
            String monthKey = yearMonth.toString();
            MonthlyAccumulator monthAcc = monthlyAccumulators.computeIfAbsent(
                monthKey, key -> new MonthlyAccumulator());
            monthAcc.trips++;
            monthAcc.amount = monthAcc.amount.add(
                booking.getTotalFare() != null ? booking.getTotalFare() : BigDecimal.ZERO);

            TrainServiceClient.TrainResponse train = resolveTrain(booking.getTrainId(), trainCache);
            if (train != null) {
                String routeKey = buildRouteKey(train);
                RouteAccumulator routeAcc = routeAccumulators.computeIfAbsent(
                    routeKey, key -> new RouteAccumulator(train.getSourceStation(), train.getDestinationStation(),
                                                          train.getDistance()));
                routeAcc.trips++;
                routeAcc.totalSpent = routeAcc.totalSpent.add(
                    booking.getTotalFare() != null ? booking.getTotalFare() : BigDecimal.ZERO);

                TrainAccumulator trainAcc = trainAccumulators.computeIfAbsent(
                    train.getId(), key -> new TrainAccumulator(train.getId(), train.getTrainName()));
                trainAcc.trips++;
                trainAcc.totalSpent = trainAcc.totalSpent.add(
                    booking.getTotalFare() != null ? booking.getTotalFare() : BigDecimal.ZERO);

                if (train.getDistance() != null) {
                    totalDistance += train.getDistance();
                }
            }
        }

        response.setConfirmedTrips(confirmedTrips);
        response.setCancelledTrips(cancelledTrips);
        response.setTotalSpent(totalSpent);
        if (response.getTotalBookings() > 0) {
            response.setAverageFare(totalSpent.divide(
                BigDecimal.valueOf(response.getTotalBookings()), 2, RoundingMode.HALF_UP));
        }
        response.setTotalDistanceKm(Math.round(totalDistance * 100.0) / 100.0);
        response.setUniqueRoutes(routeAccumulators.size());
        response.setUniqueTrains(trainAccumulators.size());

        response.setMonthlySummaries(
            monthlyAccumulators.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    TravelAnalyticsResponse.MonthlySummary summary = new TravelAnalyticsResponse.MonthlySummary();
                    summary.setMonth(entry.getKey());
                    summary.setTrips(entry.getValue().trips);
                    summary.setAmount(entry.getValue().amount);
                    return summary;
                })
                .toList()
        );

        response.setFavoriteRoutes(
            routeAccumulators.values().stream()
                .sorted(Comparator.comparingLong(RouteAccumulator::getTrips).reversed())
                .limit(MAX_TOP_ROUTES)
                .map(acc -> {
                    TravelAnalyticsResponse.RouteSummary summary = new TravelAnalyticsResponse.RouteSummary();
                    summary.setSourceStation(acc.source);
                    summary.setDestinationStation(acc.destination);
                    summary.setTrips(acc.trips);
                    summary.setTotalSpent(acc.totalSpent);
                    summary.setDistanceKm(acc.distance != null ? Double.valueOf(acc.distance) : null);
                    return summary;
                }).toList()
        );

        response.setTopTrains(
            trainAccumulators.values().stream()
                .sorted(Comparator.comparingLong(TrainAccumulator::getTrips).reversed())
                .limit(MAX_TOP_TRAINS)
                .map(acc -> {
                    TravelAnalyticsResponse.TrainSummary summary = new TravelAnalyticsResponse.TrainSummary();
                    summary.setTrainId(acc.trainId);
                    summary.setTrainName(acc.trainName);
                    summary.setTrips(acc.trips);
                    summary.setTotalSpent(acc.totalSpent);
                    return summary;
                }).toList()
        );

        return response;
    }

    public List<TravelAnalyticsResponse.RouteSummary> getFavoriteRoutes(Long userId) {
        return getTravelAnalytics(userId).getFavoriteRoutes();
    }

    public List<TravelTimelineEntry> getTimeline(Long userId) {
        List<SimpleBooking> bookings = loadUserBookings(userId);
        if (bookings.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, TrainServiceClient.TrainResponse> trainCache = new HashMap<>();
        return bookings.stream()
            .sorted(Comparator.comparing(SimpleBooking::getBookingTime,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed())
            .map(booking -> {
                TravelTimelineEntry entry = new TravelTimelineEntry();
                entry.setPnrNumber(booking.getPnrNumber());
                entry.setBookingTime(booking.getBookingTime());
                entry.setStatus(booking.getStatus());
                entry.setFare(booking.getTotalFare());
                TrainServiceClient.TrainResponse train = resolveTrain(booking.getTrainId(), trainCache);
                if (train != null) {
                    entry.setTrainName(train.getTrainName());
                    entry.setRoute(buildRouteKey(train));
                }
                return entry;
            })
            .toList();
    }

    public TravelExportResponse exportTravelHistory(Long userId, String format) {
        List<SimpleBooking> bookings = loadUserBookings(userId);
        TravelExportResponse response = new TravelExportResponse();
        String normalizedFormat = (format == null || format.isBlank()) ? "csv" : format.toLowerCase(Locale.ROOT);
        response.setFormat(normalizedFormat);
        response.setFilename("travel-history-" + userId + "." + normalizedFormat);
        response.setContentType("text/csv");

        Map<Long, TrainServiceClient.TrainResponse> trainCache = new HashMap<>();
        StringBuilder csv = new StringBuilder("PNR,Train,Route,Status,BookingTime,Fare\n");
        for (SimpleBooking booking : bookings) {
            TrainServiceClient.TrainResponse train = resolveTrain(booking.getTrainId(), trainCache);
            String route = train != null ? buildRouteKey(train) : "N/A";
            String trainName = train != null ? train.getTrainName() : "N/A";
            csv.append(sanitize(booking.getPnrNumber())).append(',')
               .append(sanitize(trainName)).append(',')
               .append(sanitize(route)).append(',')
               .append(sanitize(booking.getStatus())).append(',')
               .append(booking.getBookingTime() != null ? booking.getBookingTime() : "").append(',')
               .append(booking.getTotalFare() != null ? booking.getTotalFare() : BigDecimal.ZERO).append('\n');
        }
        response.setData(BASE64.encodeToString(csv.toString().getBytes(StandardCharsets.UTF_8)));
        return response;
    }

    private List<SimpleBooking> loadUserBookings(Long userId) {
        List<SimpleBooking> bookings = bookingRepository.findByUserId(userId);
        if (TenantContext.hasTenant()) {
            String tenantId = TenantContext.getTenantId();
            bookings = bookings.stream()
                .filter(booking -> tenantId.equals(booking.getTenantId()))
                .collect(Collectors.toList());
        }
        return bookings;
    }

    private TrainServiceClient.TrainResponse resolveTrain(Long trainId,
                                                          Map<Long, TrainServiceClient.TrainResponse> cache) {
        if (trainId == null) {
            return null;
        }
        return cache.computeIfAbsent(trainId, id -> {
            try {
                return trainServiceClient.getTrainById(id);
            } catch (Exception e) {
                logger.warn("Unable to fetch train info for id {}: {}", id, e.getMessage());
                return null;
            }
        });
    }

    private String buildRouteKey(TrainServiceClient.TrainResponse train) {
        String source = train.getSourceStation() != null ? train.getSourceStation() : "UNKNOWN";
        String destination = train.getDestinationStation() != null ? train.getDestinationStation() : "UNKNOWN";
        return source + " → " + destination;
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(',', ' ');
    }

    private static class RouteAccumulator {
        private final String source;
        private final String destination;
        private final Integer distance;
        private long trips = 0;
        private BigDecimal totalSpent = BigDecimal.ZERO;

        private RouteAccumulator(String source, String destination, Integer distance) {
            this.source = source;
            this.destination = destination;
            this.distance = distance;
        }

        public long getTrips() {
            return trips;
        }
    }

    private static class TrainAccumulator {
        private final Long trainId;
        private final String trainName;
        private long trips = 0;
        private BigDecimal totalSpent = BigDecimal.ZERO;

        private TrainAccumulator(Long trainId, String trainName) {
            this.trainId = trainId;
            this.trainName = trainName;
        }

        public long getTrips() {
            return trips;
        }
    }

    private static class MonthlyAccumulator {
        private int trips = 0;
        private BigDecimal amount = BigDecimal.ZERO;
    }
}

