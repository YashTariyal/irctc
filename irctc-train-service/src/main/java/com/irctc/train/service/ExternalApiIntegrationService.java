package com.irctc.train.service;

import com.irctc.external.railways.IndianRailwaysApiService;
import com.irctc.external.weather.WeatherApiService;
import com.irctc.external.maps.MapsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.HashMap;

/**
 * External API Integration Service for Train Service
 */
@Service
public class ExternalApiIntegrationService {

    @Autowired
    private IndianRailwaysApiService railwaysApiService;
    
    @Autowired
    private WeatherApiService weatherApiService;
    
    @Autowired
    private MapsApiService mapsApiService;

    /**
     * Get comprehensive train information with external data
     */
    public Mono<Map<String, Object>> getTrainInfoWithExternalData(String trainNumber, String date) {
        Map<String, Object> result = new HashMap<>();
        
        return railwaysApiService.getTrainStatus(trainNumber, date)
                .flatMap(trainStatus -> {
                    result.put("trainStatus", trainStatus);
                    return railwaysApiService.getTrainRunningInfo(trainNumber, date);
                })
                .flatMap(trainRunning -> {
                    result.put("trainRunning", trainRunning);
                    return railwaysApiService.getTrainDelays(trainNumber, date);
                })
                .flatMap(trainDelays -> {
                    result.put("trainDelays", trainDelays);
                    return railwaysApiService.getTrainPosition(trainNumber);
                })
                .flatMap(trainPosition -> {
                    result.put("trainPosition", trainPosition);
                    // Get weather for current location
                    if (trainPosition.getCurrentStation() != null) {
                        return weatherApiService.getCurrentWeather(trainPosition.getCurrentStation());
                    }
                    return Mono.just(new WeatherApiService.WeatherResponse());
                })
                .flatMap(weather -> {
                    result.put("weather", weather);
                    return Mono.just(result);
                })
                .onErrorResume(throwable -> {
                    System.err.println("Error getting external train data: " + throwable.getMessage());
                    return Mono.just(result);
                });
    }

    /**
     * Get route information with maps integration
     */
    public Mono<Map<String, Object>> getRouteWithMaps(String trainNumber) {
        Map<String, Object> result = new HashMap<>();
        
        return railwaysApiService.getTrainRoute(trainNumber)
                .flatMap(trainRoute -> {
                    result.put("trainRoute", trainRoute);
                    
                    if (trainRoute.getRoute() != null && trainRoute.getRoute().size() >= 2) {
                        String origin = trainRoute.getRoute().get(0).getStationName();
                        String destination = trainRoute.getRoute().get(trainRoute.getRoute().size() - 1).getStationName();
                        
                        return mapsApiService.getDirections(origin, destination, "transit");
                    }
                    return Mono.just(new MapsApiService.DirectionsResponse());
                })
                .flatMap(directions -> {
                    result.put("directions", directions);
                    return Mono.just(result);
                })
                .onErrorResume(throwable -> {
                    System.err.println("Error getting route with maps: " + throwable.getMessage());
                    return Mono.just(result);
                });
    }

    /**
     * Get weather forecast for train journey
     */
    public Mono<Map<String, Object>> getWeatherForecastForJourney(String originCity, String destinationCity) {
        Map<String, Object> result = new HashMap<>();
        
        return weatherApiService.getCurrentWeather(originCity)
                .flatMap(originWeather -> {
                    result.put("originWeather", originWeather);
                    return weatherApiService.getCurrentWeather(destinationCity);
                })
                .flatMap(destinationWeather -> {
                    result.put("destinationWeather", destinationWeather);
                    return weatherApiService.getWeatherForecast(originCity, 5);
                })
                .flatMap(forecast -> {
                    result.put("forecast", forecast);
                    return Mono.just(result);
                })
                .onErrorResume(throwable -> {
                    System.err.println("Error getting weather forecast: " + throwable.getMessage());
                    return Mono.just(result);
                });
    }

    /**
     * Get station information with external data
     */
    public Mono<Map<String, Object>> getStationInfoWithExternalData(String stationCode) {
        Map<String, Object> result = new HashMap<>();
        
        return railwaysApiService.getStationInfo(stationCode)
                .flatMap(stationInfo -> {
                    result.put("stationInfo", stationInfo);
                    
                    if (stationInfo.getCity() != null) {
                        return weatherApiService.getCurrentWeather(stationInfo.getCity());
                    }
                    return Mono.just(new WeatherApiService.WeatherResponse());
                })
                .flatMap(weather -> {
                    result.put("weather", weather);
                    // Get station info from result to access station name
                    Object stationInfoObj = result.get("stationInfo");
                    if (stationInfoObj != null) {
                        return mapsApiService.getGeocoding(stationInfoObj.toString());
                    }
                    return Mono.just(new MapsApiService.GeocodingResponse());
                })
                .flatMap(geocoding -> {
                    result.put("geocoding", geocoding);
                    return Mono.just(result);
                })
                .onErrorResume(throwable -> {
                    System.err.println("Error getting station info with external data: " + throwable.getMessage());
                    return Mono.just(result);
                });
    }

    /**
     * Search trains with external data integration
     */
    public Mono<Map<String, Object>> searchTrainsWithExternalData(String fromStation, String toStation, String date) {
        Map<String, Object> result = new HashMap<>();
        
        return railwaysApiService.searchTrains(fromStation, toStation, date)
                .flatMap(trainSearch -> {
                    result.put("trainSearch", trainSearch);
                    
                    // Get weather for both stations
                    return weatherApiService.getCurrentWeather(fromStation);
                })
                .flatMap(fromWeather -> {
                    result.put("fromWeather", fromWeather);
                    return weatherApiService.getCurrentWeather(toStation);
                })
                .flatMap(toWeather -> {
                    result.put("toWeather", toWeather);
                    return mapsApiService.getDistance(fromStation, toStation);
                })
                .flatMap(distance -> {
                    result.put("distance", distance);
                    return Mono.just(result);
                })
                .onErrorResume(throwable -> {
                    System.err.println("Error searching trains with external data: " + throwable.getMessage());
                    return Mono.just(result);
                });
    }
}
