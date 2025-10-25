package com.irctc.external.railways;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Indian Railways API Integration Service
 */
@Service
public class IndianRailwaysApiService {

    private final WebClient webClient;
    private final String railwaysApiKey;
    private final String railwaysBaseUrl;

    public IndianRailwaysApiService(WebClient.Builder webClientBuilder,
                                  @Value("${external.railways.api.key}") String railwaysApiKey,
                                  @Value("${external.railways.api.url:https://api.irctc.co.in/v1}") String railwaysBaseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(railwaysBaseUrl)
                .defaultHeaders(headers -> {
                    headers.set("Authorization", "Bearer " + railwaysApiKey);
                    headers.set("Content-Type", "application/json");
                    headers.set("X-API-Key", railwaysApiKey);
                })
                .build();
        this.railwaysApiKey = railwaysApiKey;
        this.railwaysBaseUrl = railwaysBaseUrl;
    }

    /**
     * Get live train status
     */
    public Mono<TrainStatusResponse> getTrainStatus(String trainNumber, String date) {
        return webClient.get()
                .uri("/trains/{trainNumber}/status?date={date}", trainNumber, date)
                .retrieve()
                .bodyToMono(TrainStatusResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error fetching train status: " + throwable.getMessage());
                    return Mono.just(new TrainStatusResponse());
                });
    }

    /**
     * Get train running information
     */
    public Mono<TrainRunningResponse> getTrainRunningInfo(String trainNumber, String date) {
        return webClient.get()
                .uri("/trains/{trainNumber}/running?date={date}", trainNumber, date)
                .retrieve()
                .bodyToMono(TrainRunningResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error fetching train running info: " + throwable.getMessage());
                    return Mono.just(new TrainRunningResponse());
                });
    }

    /**
     * Get train schedule
     */
    public Mono<TrainScheduleResponse> getTrainSchedule(String trainNumber) {
        return webClient.get()
                .uri("/trains/{trainNumber}/schedule", trainNumber)
                .retrieve()
                .bodyToMono(TrainScheduleResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error fetching train schedule: " + throwable.getMessage());
                    return Mono.just(new TrainScheduleResponse());
                });
    }

    /**
     * Get train route
     */
    public Mono<TrainRouteResponse> getTrainRoute(String trainNumber) {
        return webClient.get()
                .uri("/trains/{trainNumber}/route", trainNumber)
                .retrieve()
                .bodyToMono(TrainRouteResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error fetching train route: " + throwable.getMessage());
                    return Mono.just(new TrainRouteResponse());
                });
    }

    /**
     * Get live train position
     */
    public Mono<TrainPositionResponse> getTrainPosition(String trainNumber) {
        return webClient.get()
                .uri("/trains/{trainNumber}/position", trainNumber)
                .retrieve()
                .bodyToMono(TrainPositionResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error fetching train position: " + throwable.getMessage());
                    return Mono.just(new TrainPositionResponse());
                });
    }

    /**
     * Get train delays
     */
    public Mono<TrainDelaysResponse> getTrainDelays(String trainNumber, String date) {
        return webClient.get()
                .uri("/trains/{trainNumber}/delays?date={date}", trainNumber, date)
                .retrieve()
                .bodyToMono(TrainDelaysResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error fetching train delays: " + throwable.getMessage());
                    return Mono.just(new TrainDelaysResponse());
                });
    }

    /**
     * Get station information
     */
    public Mono<StationInfoResponse> getStationInfo(String stationCode) {
        return webClient.get()
                .uri("/stations/{stationCode}", stationCode)
                .retrieve()
                .bodyToMono(StationInfoResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error fetching station info: " + throwable.getMessage());
                    return Mono.just(new StationInfoResponse());
                });
    }

    /**
     * Search trains between stations
     */
    public Mono<TrainSearchResponse> searchTrains(String fromStation, String toStation, String date) {
        return webClient.get()
                .uri("/trains/search?from={from}&to={to}&date={date}", fromStation, toStation, date)
                .retrieve()
                .bodyToMono(TrainSearchResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error searching trains: " + throwable.getMessage());
                    return Mono.just(new TrainSearchResponse());
                });
    }

    // Response DTOs
    public static class TrainStatusResponse {
        @JsonProperty("trainNumber")
        private String trainNumber;
        
        @JsonProperty("trainName")
        private String trainName;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("currentStation")
        private String currentStation;
        
        @JsonProperty("nextStation")
        private String nextStation;
        
        @JsonProperty("delay")
        private Integer delay;
        
        @JsonProperty("lastUpdated")
        private LocalDateTime lastUpdated;

        // Getters and Setters
        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
        
        public String getTrainName() { return trainName; }
        public void setTrainName(String trainName) { this.trainName = trainName; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getCurrentStation() { return currentStation; }
        public void setCurrentStation(String currentStation) { this.currentStation = currentStation; }
        
        public String getNextStation() { return nextStation; }
        public void setNextStation(String nextStation) { this.nextStation = nextStation; }
        
        public Integer getDelay() { return delay; }
        public void setDelay(Integer delay) { this.delay = delay; }
        
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }

    public static class TrainRunningResponse {
        @JsonProperty("trainNumber")
        private String trainNumber;
        
        @JsonProperty("isRunning")
        private boolean isRunning;
        
        @JsonProperty("currentSpeed")
        private Integer currentSpeed;
        
        @JsonProperty("currentLocation")
        private String currentLocation;
        
        @JsonProperty("expectedArrival")
        private LocalDateTime expectedArrival;
        
        @JsonProperty("actualArrival")
        private LocalDateTime actualArrival;

        // Getters and Setters
        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
        
        public boolean isRunning() { return isRunning; }
        public void setRunning(boolean running) { isRunning = running; }
        
        public Integer getCurrentSpeed() { return currentSpeed; }
        public void setCurrentSpeed(Integer currentSpeed) { this.currentSpeed = currentSpeed; }
        
        public String getCurrentLocation() { return currentLocation; }
        public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }
        
        public LocalDateTime getExpectedArrival() { return expectedArrival; }
        public void setExpectedArrival(LocalDateTime expectedArrival) { this.expectedArrival = expectedArrival; }
        
        public LocalDateTime getActualArrival() { return actualArrival; }
        public void setActualArrival(LocalDateTime actualArrival) { this.actualArrival = actualArrival; }
    }

    public static class TrainScheduleResponse {
        @JsonProperty("trainNumber")
        private String trainNumber;
        
        @JsonProperty("trainName")
        private String trainName;
        
        @JsonProperty("stops")
        private List<TrainStop> stops;

        // Getters and Setters
        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
        
        public String getTrainName() { return trainName; }
        public void setTrainName(String trainName) { this.trainName = trainName; }
        
        public List<TrainStop> getStops() { return stops; }
        public void setStops(List<TrainStop> stops) { this.stops = stops; }
    }

    public static class TrainStop {
        @JsonProperty("stationCode")
        private String stationCode;
        
        @JsonProperty("stationName")
        private String stationName;
        
        @JsonProperty("arrivalTime")
        private String arrivalTime;
        
        @JsonProperty("departureTime")
        private String departureTime;
        
        @JsonProperty("distance")
        private Integer distance;
        
        @JsonProperty("day")
        private Integer day;

        // Getters and Setters
        public String getStationCode() { return stationCode; }
        public void setStationCode(String stationCode) { this.stationCode = stationCode; }
        
        public String getStationName() { return stationName; }
        public void setStationName(String stationName) { this.stationName = stationName; }
        
        public String getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
        
        public String getDepartureTime() { return departureTime; }
        public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
        
        public Integer getDistance() { return distance; }
        public void setDistance(Integer distance) { this.distance = distance; }
        
        public Integer getDay() { return day; }
        public void setDay(Integer day) { this.day = day; }
    }

    public static class TrainRouteResponse {
        @JsonProperty("trainNumber")
        private String trainNumber;
        
        @JsonProperty("route")
        private List<RouteStation> route;

        // Getters and Setters
        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
        
        public List<RouteStation> getRoute() { return route; }
        public void setRoute(List<RouteStation> route) { this.route = route; }
    }

    public static class RouteStation {
        @JsonProperty("stationCode")
        private String stationCode;
        
        @JsonProperty("stationName")
        private String stationName;
        
        @JsonProperty("latitude")
        private Double latitude;
        
        @JsonProperty("longitude")
        private Double longitude;
        
        @JsonProperty("distance")
        private Integer distance;

        // Getters and Setters
        public String getStationCode() { return stationCode; }
        public void setStationCode(String stationCode) { this.stationCode = stationCode; }
        
        public String getStationName() { return stationName; }
        public void setStationName(String stationName) { this.stationName = stationName; }
        
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        
        public Integer getDistance() { return distance; }
        public void setDistance(Integer distance) { this.distance = distance; }
    }

    public static class TrainPositionResponse {
        @JsonProperty("trainNumber")
        private String trainNumber;
        
        @JsonProperty("latitude")
        private Double latitude;
        
        @JsonProperty("longitude")
        private Double longitude;
        
        @JsonProperty("currentStation")
        private String currentStation;
        
        @JsonProperty("nextStation")
        private String nextStation;
        
        @JsonProperty("lastUpdated")
        private LocalDateTime lastUpdated;

        // Getters and Setters
        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
        
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        
        public String getCurrentStation() { return currentStation; }
        public void setCurrentStation(String currentStation) { this.currentStation = currentStation; }
        
        public String getNextStation() { return nextStation; }
        public void setNextStation(String nextStation) { this.nextStation = nextStation; }
        
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }

    public static class TrainDelaysResponse {
        @JsonProperty("trainNumber")
        private String trainNumber;
        
        @JsonProperty("delays")
        private List<TrainDelay> delays;

        // Getters and Setters
        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
        
        public List<TrainDelay> getDelays() { return delays; }
        public void setDelays(List<TrainDelay> delays) { this.delays = delays; }
    }

    public static class TrainDelay {
        @JsonProperty("stationCode")
        private String stationCode;
        
        @JsonProperty("stationName")
        private String stationName;
        
        @JsonProperty("delayMinutes")
        private Integer delayMinutes;
        
        @JsonProperty("reason")
        private String reason;

        // Getters and Setters
        public String getStationCode() { return stationCode; }
        public void setStationCode(String stationCode) { this.stationCode = stationCode; }
        
        public String getStationName() { return stationName; }
        public void setStationName(String stationName) { this.stationName = stationName; }
        
        public Integer getDelayMinutes() { return delayMinutes; }
        public void setDelayMinutes(Integer delayMinutes) { this.delayMinutes = delayMinutes; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class StationInfoResponse {
        @JsonProperty("stationCode")
        private String stationCode;
        
        @JsonProperty("stationName")
        private String stationName;
        
        @JsonProperty("city")
        private String city;
        
        @JsonProperty("state")
        private String state;
        
        @JsonProperty("latitude")
        private Double latitude;
        
        @JsonProperty("longitude")
        private Double longitude;
        
        @JsonProperty("platforms")
        private Integer platforms;

        // Getters and Setters
        public String getStationCode() { return stationCode; }
        public void setStationCode(String stationCode) { this.stationCode = stationCode; }
        
        public String getStationName() { return stationName; }
        public void setStationName(String stationName) { this.stationName = stationName; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        
        public Integer getPlatforms() { return platforms; }
        public void setPlatforms(Integer platforms) { this.platforms = platforms; }
    }

    public static class TrainSearchResponse {
        @JsonProperty("trains")
        private List<TrainSearchResult> trains;
        
        @JsonProperty("totalCount")
        private Integer totalCount;

        // Getters and Setters
        public List<TrainSearchResult> getTrains() { return trains; }
        public void setTrains(List<TrainSearchResult> trains) { this.trains = trains; }
        
        public Integer getTotalCount() { return totalCount; }
        public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    }

    public static class TrainSearchResult {
        @JsonProperty("trainNumber")
        private String trainNumber;
        
        @JsonProperty("trainName")
        private String trainName;
        
        @JsonProperty("fromStation")
        private String fromStation;
        
        @JsonProperty("toStation")
        private String toStation;
        
        @JsonProperty("departureTime")
        private String departureTime;
        
        @JsonProperty("arrivalTime")
        private String arrivalTime;
        
        @JsonProperty("duration")
        private String duration;
        
        @JsonProperty("distance")
        private Integer distance;

        // Getters and Setters
        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
        
        public String getTrainName() { return trainName; }
        public void setTrainName(String trainName) { this.trainName = trainName; }
        
        public String getFromStation() { return fromStation; }
        public void setFromStation(String fromStation) { this.fromStation = fromStation; }
        
        public String getToStation() { return toStation; }
        public void setToStation(String toStation) { this.toStation = toStation; }
        
        public String getDepartureTime() { return departureTime; }
        public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
        
        public String getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
        
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        
        public Integer getDistance() { return distance; }
        public void setDistance(Integer distance) { this.distance = distance; }
    }
}
