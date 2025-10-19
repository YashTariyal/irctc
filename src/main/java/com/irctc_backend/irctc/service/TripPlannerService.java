package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.dto.TripPlanRequest;
import com.irctc_backend.irctc.dto.TripPlanResponse;
import com.irctc_backend.irctc.entity.Station;
import com.irctc_backend.irctc.entity.Train;
import com.irctc_backend.irctc.repository.StationRepository;
import com.irctc_backend.irctc.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class TripPlannerService {

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private StationRepository stationRepository;

    public List<TripPlanResponse> findItineraries(TripPlanRequest request) {
        Station source = stationRepository.findByStationCode(request.getSourceStationCode())
                .orElseThrow(() -> new RuntimeException("Source station not found"));
        Station destination = stationRepository.findByStationCode(request.getDestinationStationCode())
                .orElseThrow(() -> new RuntimeException("Destination station not found"));

        LocalDate date = request.getJourneyDate();
        LocalTime earliest = Optional.ofNullable(request.getEarliestDeparture()).orElse(LocalTime.MIN);
        LocalTime latest = Optional.ofNullable(request.getLatestArrival()).orElse(LocalTime.MAX);

        // Simple heuristic: try direct trains first
        List<TripPlanResponse> results = new ArrayList<>();
        List<Train> allTrains = trainRepository.findAll();

        for (Train train : allTrains) {
            if (Objects.equals(train.getSourceStation(), source) && Objects.equals(train.getDestinationStation(), destination)) {
                if (!train.getDepartureTime().isBefore(earliest) && !train.getArrivalTime().isAfter(latest)) {
                    results.add(buildDirectResponse(train, source, destination, date));
                }
            }
        }

        if (results.isEmpty() && request.getMaxConnections() != null && request.getMaxConnections() > 0) {
            // Try 1-connection itineraries: source -> X -> destination
            for (Train first : allTrains) {
                if (!Objects.equals(first.getSourceStation(), source)) continue;
                Station mid = first.getDestinationStation();
                if (mid == null || Objects.equals(mid, source) || Objects.equals(mid, destination)) continue;
                if (first.getDepartureTime().isBefore(earliest)) continue;

                for (Train second : allTrains) {
                    if (!Objects.equals(second.getSourceStation(), mid)) continue;
                    if (!Objects.equals(second.getDestinationStation(), destination)) continue;

                    // Ensure feasible connection time (simple: second departs after first arrives)
                    if (!second.getDepartureTime().isAfter(first.getArrivalTime())) continue;
                    if (second.getArrivalTime().isAfter(latest)) continue;

                    TripPlanResponse resp = new TripPlanResponse();
                    resp.setSourceStationCode(source.getStationCode());
                    resp.setDestinationStationCode(destination.getStationCode());
                    resp.setJourneyDate(date);

                    List<TripPlanResponse.TripLeg> legs = new ArrayList<>();
                    legs.add(toLeg(first, source, mid));
                    legs.add(toLeg(second, mid, destination));
                    resp.setLegs(legs);

                    int totalDuration = durationMinutes(first.getDepartureTime(), first.getArrivalTime())
                            + durationMinutes(second.getDepartureTime(), second.getArrivalTime());
                    resp.setTotalDurationMinutes(totalDuration);
                    resp.setTotalConnections(1);
                    resp.setOvernight(isOvernight(first.getDepartureTime(), second.getArrivalTime()));
                    resp.setEstimatedFare(estimateFare(first, second));
                    results.add(resp);
                }
            }
        }

        // Sort by duration or heuristic
        results.sort(Comparator.comparing(TripPlanResponse::getTotalDurationMinutes));
        return results;
    }

    private TripPlanResponse buildDirectResponse(Train train, Station source, Station destination, LocalDate date) {
        TripPlanResponse resp = new TripPlanResponse();
        resp.setSourceStationCode(source.getStationCode());
        resp.setDestinationStationCode(destination.getStationCode());
        resp.setJourneyDate(date);
        resp.setTotalConnections(0);
        resp.setOvernight(isOvernight(train.getDepartureTime(), train.getArrivalTime()));
        resp.setTotalDurationMinutes(durationMinutes(train.getDepartureTime(), train.getArrivalTime()));
        resp.setEstimatedFare(estimateFare(train));

        TripPlanResponse.TripLeg leg = toLeg(train, source, destination);
        resp.setLegs(Collections.singletonList(leg));
        return resp;
    }

    private TripPlanResponse.TripLeg toLeg(Train train, Station from, Station to) {
        TripPlanResponse.TripLeg leg = new TripPlanResponse.TripLeg();
        leg.setTrainId(train.getId());
        leg.setTrainNumber(train.getTrainNumber());
        leg.setTrainName(train.getTrainName());
        leg.setFromStationCode(from.getStationCode());
        leg.setFromStationName(from.getStationName());
        leg.setToStationCode(to.getStationCode());
        leg.setToStationName(to.getStationName());
        leg.setDepartureTime(train.getDepartureTime());
        leg.setArrivalTime(train.getArrivalTime());
        leg.setDurationMinutes(durationMinutes(train.getDepartureTime(), train.getArrivalTime()));
        leg.setCoachType("GENERAL");
        leg.setTatkalAvailable(Boolean.TRUE);
        return leg;
    }

    private int durationMinutes(LocalTime start, LocalTime end) {
        int s = start.getHour() * 60 + start.getMinute();
        int e = end.getHour() * 60 + end.getMinute();
        if (e < s) {
            // overnight wrap
            e += 24 * 60;
        }
        return e - s;
    }

    private boolean isOvernight(LocalTime start, LocalTime end) {
        return end.isBefore(start);
    }

    private BigDecimal estimateFare(Train... trains) {
        // very naive: base per leg; can be wired to FareCalculationService later
        BigDecimal total = BigDecimal.ZERO;
        for (Train t : trains) {
            // assume nominal per leg cost based on train type
            BigDecimal base = switch (t.getTrainType()) {
                case RAJDHANI -> new BigDecimal("1500");
                case EXPRESS -> new BigDecimal("800");
                default -> new BigDecimal("600");
            };
            total = total.add(base);
        }
        return total;
    }
}


