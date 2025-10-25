package com.irctc.external.maps;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Google Maps API Integration Service
 */
@Service
public class MapsApiService {

    private final WebClient webClient;
    private final String mapsApiKey;
    private final String mapsBaseUrl;

    public MapsApiService(WebClient.Builder webClientBuilder,
                        @Value("${external.maps.api.key}") String mapsApiKey,
                        @Value("${external.maps.api.url:https://maps.googleapis.com/maps/api}") String mapsBaseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(mapsBaseUrl)
                .build();
        this.mapsApiKey = mapsApiKey;
        this.mapsBaseUrl = mapsBaseUrl;
    }

    /**
     * Get directions between two points
     */
    public Mono<DirectionsResponse> getDirections(String origin, String destination, String mode) {
        return webClient.get()
                .uri("/directions/json?origin={origin}&destination={destination}&mode={mode}&key={apiKey}", 
                     origin, destination, mode, mapsApiKey)
                .retrieve()
                .bodyToMono(DirectionsResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error fetching directions: " + throwable.getMessage());
                    return Mono.just(new DirectionsResponse());
                });
    }

    /**
     * Get distance between two points
     */
    public Mono<DistanceMatrixResponse> getDistance(String origin, String destination) {
        return webClient.get()
                .uri("/distancematrix/json?origins={origin}&destinations={destination}&key={apiKey}", 
                     origin, destination, mapsApiKey)
                .retrieve()
                .bodyToMono(DistanceMatrixResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error fetching distance: " + throwable.getMessage());
                    return Mono.just(new DistanceMatrixResponse());
                });
    }

    /**
     * Get place details
     */
    public Mono<PlaceDetailsResponse> getPlaceDetails(String placeId) {
        return webClient.get()
                .uri("/place/details/json?place_id={placeId}&key={apiKey}", placeId, mapsApiKey)
                .retrieve()
                .bodyToMono(PlaceDetailsResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error fetching place details: " + throwable.getMessage());
                    return Mono.just(new PlaceDetailsResponse());
                });
    }

    /**
     * Search for places
     */
    public Mono<PlacesSearchResponse> searchPlaces(String query, String location, int radius) {
        return webClient.get()
                .uri("/place/textsearch/json?query={query}&location={location}&radius={radius}&key={apiKey}", 
                     query, location, radius, mapsApiKey)
                .retrieve()
                .bodyToMono(PlacesSearchResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error searching places: " + throwable.getMessage());
                    return Mono.just(new PlacesSearchResponse());
                });
    }

    /**
     * Get geocoding information
     */
    public Mono<GeocodingResponse> getGeocoding(String address) {
        return webClient.get()
                .uri("/geocode/json?address={address}&key={apiKey}", address, mapsApiKey)
                .retrieve()
                .bodyToMono(GeocodingResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error fetching geocoding: " + throwable.getMessage());
                    return Mono.just(new GeocodingResponse());
                });
    }

    // Response DTOs
    public static class DirectionsResponse {
        @JsonProperty("routes")
        private List<Route> routes;
        
        @JsonProperty("status")
        private String status;

        // Getters and Setters
        public List<Route> getRoutes() { return routes; }
        public void setRoutes(List<Route> routes) { this.routes = routes; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class Route {
        @JsonProperty("legs")
        private List<Leg> legs;
        
        @JsonProperty("overview_polyline")
        private OverviewPolyline overviewPolyline;

        // Getters and Setters
        public List<Leg> getLegs() { return legs; }
        public void setLegs(List<Leg> legs) { this.legs = legs; }
        
        public OverviewPolyline getOverviewPolyline() { return overviewPolyline; }
        public void setOverviewPolyline(OverviewPolyline overviewPolyline) { this.overviewPolyline = overviewPolyline; }
    }

    public static class Leg {
        @JsonProperty("distance")
        private Distance distance;
        
        @JsonProperty("duration")
        private Duration duration;
        
        @JsonProperty("start_address")
        private String startAddress;
        
        @JsonProperty("end_address")
        private String endAddress;

        // Getters and Setters
        public Distance getDistance() { return distance; }
        public void setDistance(Distance distance) { this.distance = distance; }
        
        public Duration getDuration() { return duration; }
        public void setDuration(Duration duration) { this.duration = duration; }
        
        public String getStartAddress() { return startAddress; }
        public void setStartAddress(String startAddress) { this.startAddress = startAddress; }
        
        public String getEndAddress() { return endAddress; }
        public void setEndAddress(String endAddress) { this.endAddress = endAddress; }
    }

    public static class Distance {
        @JsonProperty("text")
        private String text;
        
        @JsonProperty("value")
        private Integer value;

        // Getters and Setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }
    }

    public static class Duration {
        @JsonProperty("text")
        private String text;
        
        @JsonProperty("value")
        private Integer value;

        // Getters and Setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }
    }

    public static class OverviewPolyline {
        @JsonProperty("points")
        private String points;

        // Getters and Setters
        public String getPoints() { return points; }
        public void setPoints(String points) { this.points = points; }
    }

    public static class DistanceMatrixResponse {
        @JsonProperty("rows")
        private List<Row> rows;
        
        @JsonProperty("status")
        private String status;

        // Getters and Setters
        public List<Row> getRows() { return rows; }
        public void setRows(List<Row> rows) { this.rows = rows; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class Row {
        @JsonProperty("elements")
        private List<Element> elements;

        // Getters and Setters
        public List<Element> getElements() { return elements; }
        public void setElements(List<Element> elements) { this.elements = elements; }
    }

    public static class Element {
        @JsonProperty("distance")
        private Distance distance;
        
        @JsonProperty("duration")
        private Duration duration;
        
        @JsonProperty("status")
        private String status;

        // Getters and Setters
        public Distance getDistance() { return distance; }
        public void setDistance(Distance distance) { this.distance = distance; }
        
        public Duration getDuration() { return duration; }
        public void setDuration(Duration duration) { this.duration = duration; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class PlaceDetailsResponse {
        @JsonProperty("result")
        private PlaceDetails result;
        
        @JsonProperty("status")
        private String status;

        // Getters and Setters
        public PlaceDetails getResult() { return result; }
        public void setResult(PlaceDetails result) { this.result = result; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class PlaceDetails {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("formatted_address")
        private String formattedAddress;
        
        @JsonProperty("geometry")
        private Geometry geometry;
        
        @JsonProperty("rating")
        private Double rating;
        
        @JsonProperty("types")
        private List<String> types;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getFormattedAddress() { return formattedAddress; }
        public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
        
        public Geometry getGeometry() { return geometry; }
        public void setGeometry(Geometry geometry) { this.geometry = geometry; }
        
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        
        public List<String> getTypes() { return types; }
        public void setTypes(List<String> types) { this.types = types; }
    }

    public static class Geometry {
        @JsonProperty("location")
        private Location location;

        // Getters and Setters
        public Location getLocation() { return location; }
        public void setLocation(Location location) { this.location = location; }
    }

    public static class Location {
        @JsonProperty("lat")
        private Double latitude;
        
        @JsonProperty("lng")
        private Double longitude;

        // Getters and Setters
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
    }

    public static class PlacesSearchResponse {
        @JsonProperty("results")
        private List<Place> results;
        
        @JsonProperty("status")
        private String status;

        // Getters and Setters
        public List<Place> getResults() { return results; }
        public void setResults(List<Place> results) { this.results = results; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class Place {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("formatted_address")
        private String formattedAddress;
        
        @JsonProperty("geometry")
        private Geometry geometry;
        
        @JsonProperty("rating")
        private Double rating;
        
        @JsonProperty("place_id")
        private String placeId;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getFormattedAddress() { return formattedAddress; }
        public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
        
        public Geometry getGeometry() { return geometry; }
        public void setGeometry(Geometry geometry) { this.geometry = geometry; }
        
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        
        public String getPlaceId() { return placeId; }
        public void setPlaceId(String placeId) { this.placeId = placeId; }
    }

    public static class GeocodingResponse {
        @JsonProperty("results")
        private List<GeocodingResult> results;
        
        @JsonProperty("status")
        private String status;

        // Getters and Setters
        public List<GeocodingResult> getResults() { return results; }
        public void setResults(List<GeocodingResult> results) { this.results = results; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class GeocodingResult {
        @JsonProperty("formatted_address")
        private String formattedAddress;
        
        @JsonProperty("geometry")
        private Geometry geometry;
        
        @JsonProperty("place_id")
        private String placeId;

        // Getters and Setters
        public String getFormattedAddress() { return formattedAddress; }
        public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
        
        public Geometry getGeometry() { return geometry; }
        public void setGeometry(Geometry geometry) { this.geometry = geometry; }
        
        public String getPlaceId() { return placeId; }
        public void setPlaceId(String placeId) { this.placeId = placeId; }
    }
}
