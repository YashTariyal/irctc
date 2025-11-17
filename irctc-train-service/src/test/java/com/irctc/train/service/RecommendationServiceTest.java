package com.irctc.train.service;

import com.irctc.train.dto.RecommendationRequest;
import com.irctc.train.dto.RecommendationResponse;
import com.irctc.train.entity.SimpleTrain;
import com.irctc.train.repository.SimpleTrainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private SimpleTrainRepository trainRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private SimpleTrain fastTrain;
    private SimpleTrain cheapTrain;

    @BeforeEach
    void setUp() {
        fastTrain = buildTrain(1L, "12345", "Express Fast",
            LocalDateTime.of(2025, 1, 1, 8, 0),
            LocalDateTime.of(2025, 1, 1, 16, 0),
            "RAJDHANI", "1A", 2500.0, 100, 80);

        cheapTrain = buildTrain(2L, "54321", "Budget Express",
            LocalDateTime.of(2025, 1, 1, 22, 0),
            LocalDateTime.of(2025, 1, 2, 10, 0),
            "EXPRESS", "3A", 700.0, 500, 400);

        when(trainRepository.findBySourceStationAndDestinationStation(anyString(), anyString()))
            .thenReturn(List.of(fastTrain, cheapTrain));
    }

    @Test
    void shouldPrioritizeAvailabilityAndFare() {
        RecommendationRequest request = new RecommendationRequest();
        request.setUserId(10L);
        request.setSourceStation("NDLS");
        request.setDestinationStation("BCT");

        List<RecommendationResponse> recommendations = recommendationService.recommendTrains(request);

        assertThat(recommendations).hasSize(2);
        assertThat(recommendations.get(0).getTrainNumber()).isEqualTo("54321"); // cheaper & high availability
        assertThat(recommendations.get(0).getAvailabilityScore()).isGreaterThan(0);
    }

    @Test
    void shouldConsiderPreferredTrainTypeAndSeatClass() {
        RecommendationRequest request = new RecommendationRequest();
        request.setUserId(10L);
        request.setSourceStation("NDLS");
        request.setDestinationStation("BCT");
        request.setPreferredTrainTypes(List.of("RAJDHANI"));
        request.setSeatClass("1A");
        request.setPreferredDepartureTime(LocalTime.of(8, 0));

        List<RecommendationResponse> recommendations = recommendationService.recommendTrains(request);

        assertThat(recommendations.get(0).getTrainNumber()).isEqualTo("12345");
        assertThat(recommendations.get(0).getRecommendationReason()).contains("Preferred train type");
    }

    @Test
    void shouldReturnEmptyWhenNoTrains() {
        when(trainRepository.findBySourceStationAndDestinationStation("DEL", "BCT"))
            .thenReturn(List.of());

        RecommendationRequest request = new RecommendationRequest();
        request.setSourceStation("DEL");
        request.setDestinationStation("BCT");

        List<RecommendationResponse> recommendations = recommendationService.recommendTrains(request);
        assertThat(recommendations).isEmpty();
    }

    private SimpleTrain buildTrain(Long id, String number, String name,
                                   LocalDateTime departure, LocalDateTime arrival,
                                   String type, String seatClass,
                                   Double baseFare, Integer totalSeats, Integer availableSeats) {
        SimpleTrain train = new SimpleTrain();
        train.setId(id);
        train.setTrainNumber(number);
        train.setTrainName(name);
        train.setSourceStation("NDLS");
        train.setDestinationStation("BCT");
        train.setDepartureTime(departure);
        train.setArrivalTime(arrival);
        train.setTrainType(type);
        train.setTrainClass(seatClass);
        train.setBaseFare(baseFare);
        train.setTotalSeats(totalSeats);
        train.setAvailableSeats(availableSeats);
        return train;
    }
}

