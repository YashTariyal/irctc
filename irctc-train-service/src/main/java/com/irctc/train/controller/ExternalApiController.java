package com.irctc.train.controller;

import com.irctc.train.service.ExternalApiIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * External API Integration Controller
 */
@RestController
@RequestMapping("/api/trains/external")
public class ExternalApiController {

    @Autowired
    private ExternalApiIntegrationService externalApiService;

    /**
     * Get train information with external data
     */
    @GetMapping("/{trainNumber}/info")
    public Mono<ResponseEntity<Map<String, Object>>> getTrainInfoWithExternalData(
            @PathVariable String trainNumber,
            @RequestParam String date) {
        
        return externalApiService.getTrainInfoWithExternalData(trainNumber, date)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Get route information with maps
     */
    @GetMapping("/{trainNumber}/route")
    public Mono<ResponseEntity<Map<String, Object>>> getRouteWithMaps(
            @PathVariable String trainNumber) {
        
        return externalApiService.getRouteWithMaps(trainNumber)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Get weather forecast for journey
     */
    @GetMapping("/weather/forecast")
    public Mono<ResponseEntity<Map<String, Object>>> getWeatherForecastForJourney(
            @RequestParam String originCity,
            @RequestParam String destinationCity) {
        
        return externalApiService.getWeatherForecastForJourney(originCity, destinationCity)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Get station information with external data
     */
    @GetMapping("/stations/{stationCode}/info")
    public Mono<ResponseEntity<Map<String, Object>>> getStationInfoWithExternalData(
            @PathVariable String stationCode) {
        
        return externalApiService.getStationInfoWithExternalData(stationCode)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Search trains with external data
     */
    @GetMapping("/search")
    public Mono<ResponseEntity<Map<String, Object>>> searchTrainsWithExternalData(
            @RequestParam String fromStation,
            @RequestParam String toStation,
            @RequestParam String date) {
        
        return externalApiService.searchTrainsWithExternalData(fromStation, toStation, date)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
}
