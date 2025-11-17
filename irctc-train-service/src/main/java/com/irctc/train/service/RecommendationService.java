package com.irctc.train.service;

import com.irctc.train.dto.RecommendationRequest;
import com.irctc.train.dto.RecommendationResponse;
import com.irctc.train.entity.SimpleTrain;
import com.irctc.train.repository.SimpleTrainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    private final SimpleTrainRepository trainRepository;

    public RecommendationService(SimpleTrainRepository trainRepository) {
        this.trainRepository = trainRepository;
    }

    @Cacheable(value = "train-recommendations", key = "#request.userId + ':' + #request.sourceStation + ':' + #request.destinationStation")
    public List<RecommendationResponse> recommendTrains(RecommendationRequest request) {
        logger.info("Generating recommendations for user {} from {} to {}", request.getUserId(),
            request.getSourceStation(), request.getDestinationStation());

        List<SimpleTrain> trains = trainRepository.findBySourceStationAndDestinationStation(
            request.getSourceStation(), request.getDestinationStation());

        if (trains.isEmpty()) {
            logger.info("No direct trains found between {} and {}", request.getSourceStation(), request.getDestinationStation());
            return List.of();
        }

        return trains.stream()
            .map(train -> scoreTrain(train, request))
            .sorted(Comparator.comparingDouble(RecommendationResponse::getAvailabilityScore).reversed())
            .limit(10)
            .collect(Collectors.toList());
    }

    private RecommendationResponse scoreTrain(SimpleTrain train, RecommendationRequest request) {
        double score = 0.0;
        StringBuilder reason = new StringBuilder();

        if (train.getAvailableSeats() != null && train.getTotalSeats() != null && train.getTotalSeats() > 0) {
            double availability = (double) train.getAvailableSeats() / train.getTotalSeats();
            score += availability * 40;
            reason.append("Availability ").append(String.format(Locale.ENGLISH, "%.0f%%", availability * 100)).append(". ");
        }

        if (train.getBaseFare() != null && train.getBaseFare() > 0) {
            double fareScore = Math.max(0, 1 - (train.getBaseFare() / 2000.0));
            score += fareScore * 25;
            reason.append("Fare ₹").append(train.getBaseFare().intValue()).append(". ");
        }

        if (request.getPreferredDepartureTime() != null && train.getDepartureTime() != null) {
            LocalTime dep = train.getDepartureTime().toLocalTime();
            long diff = Math.abs(ChronoUnit.MINUTES.between(dep, request.getPreferredDepartureTime()));
            double timeScore = Math.max(0, 1 - (diff / 720.0));
            score += timeScore * 20;
            if (diff <= 120) {
                reason.append("Matches preferred time. ");
            }
        }

        if (request.getPreferredTrainTypes() != null && !request.getPreferredTrainTypes().isEmpty()
            && train.getTrainType() != null) {
            if (request.getPreferredTrainTypes().stream()
                .anyMatch(type -> type.equalsIgnoreCase(train.getTrainType()))) {
                score += 10;
                reason.append("Preferred train type. ");
            }
        }

        if (request.getSeatClass() != null && train.getTrainClass() != null &&
            request.getSeatClass().equalsIgnoreCase(train.getTrainClass())) {
            score += 5;
            reason.append("Matches seat class preference. ");
        }

        if (score == 0) {
            score = 10;
            reason.append("Fallback recommendation. ");
        }

        RecommendationResponse response = new RecommendationResponse();
        response.setTrainId(train.getId());
        response.setTrainNumber(train.getTrainNumber());
        response.setTrainName(train.getTrainName());
        response.setSourceStation(train.getSourceStation());
        response.setDestinationStation(train.getDestinationStation());
        response.setDepartureTime(train.getDepartureTime());
        response.setArrivalTime(train.getArrivalTime());
        response.setDurationHours(train.getDuration() != null ? train.getDuration() / 60.0 : null);
        response.setDistance(train.getDistance());
        response.setAvailabilityScore(Math.round(score * 100.0) / 100.0);
        response.setRecommendationReason(reason.toString().trim());
        if (train.getBaseFare() != null) {
            response.setPredictedFare(BigDecimal.valueOf(train.getBaseFare())
                .setScale(2, RoundingMode.HALF_UP));
        }
        return response;
    }
}

