package com.irctc.external.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Weather API Integration Service
 */
@Service
public class WeatherApiService {

    private final WebClient webClient;
    private final String weatherApiKey;
    private final String weatherBaseUrl;

    public WeatherApiService(WebClient.Builder webClientBuilder,
                           @Value("${external.weather.api.key}") String weatherApiKey,
                           @Value("${external.weather.api.url:https://api.openweathermap.org/data/2.5}") String weatherBaseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(weatherBaseUrl)
                .build();
        this.weatherApiKey = weatherApiKey;
        this.weatherBaseUrl = weatherBaseUrl;
    }

    /**
     * Get current weather for a city
     */
    public Mono<WeatherResponse> getCurrentWeather(String city) {
        return webClient.get()
                .uri("/weather?q={city}&appid={apiKey}&units=metric", city, weatherApiKey)
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error fetching weather: " + throwable.getMessage());
                    return Mono.just(new WeatherResponse());
                });
    }

    /**
     * Get weather forecast for a city
     */
    public Mono<WeatherForecastResponse> getWeatherForecast(String city, int days) {
        return webClient.get()
                .uri("/forecast?q={city}&appid={apiKey}&units=metric&cnt={days}", city, weatherApiKey, days)
                .retrieve()
                .bodyToMono(WeatherForecastResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error fetching weather forecast: " + throwable.getMessage());
                    return Mono.just(new WeatherForecastResponse());
                });
    }

    /**
     * Get weather by coordinates
     */
    public Mono<WeatherResponse> getWeatherByCoordinates(double latitude, double longitude) {
        return webClient.get()
                .uri("/weather?lat={lat}&lon={lon}&appid={apiKey}&units=metric", latitude, longitude, weatherApiKey)
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error fetching weather by coordinates: " + throwable.getMessage());
                    return Mono.just(new WeatherResponse());
                });
    }

    // Response DTOs
    public static class WeatherResponse {
        @JsonProperty("main")
        private WeatherMain main;
        
        @JsonProperty("weather")
        private List<WeatherCondition> weather;
        
        @JsonProperty("wind")
        private Wind wind;
        
        @JsonProperty("visibility")
        private Integer visibility;
        
        @JsonProperty("name")
        private String cityName;
        
        @JsonProperty("dt")
        private Long timestamp;

        // Getters and Setters
        public WeatherMain getMain() { return main; }
        public void setMain(WeatherMain main) { this.main = main; }
        
        public List<WeatherCondition> getWeather() { return weather; }
        public void setWeather(List<WeatherCondition> weather) { this.weather = weather; }
        
        public Wind getWind() { return wind; }
        public void setWind(Wind wind) { this.wind = wind; }
        
        public Integer getVisibility() { return visibility; }
        public void setVisibility(Integer visibility) { this.visibility = visibility; }
        
        public String getCityName() { return cityName; }
        public void setCityName(String cityName) { this.cityName = cityName; }
        
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }

    public static class WeatherMain {
        @JsonProperty("temp")
        private Double temperature;
        
        @JsonProperty("feels_like")
        private Double feelsLike;
        
        @JsonProperty("temp_min")
        private Double minTemperature;
        
        @JsonProperty("temp_max")
        private Double maxTemperature;
        
        @JsonProperty("pressure")
        private Integer pressure;
        
        @JsonProperty("humidity")
        private Integer humidity;

        // Getters and Setters
        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
        
        public Double getFeelsLike() { return feelsLike; }
        public void setFeelsLike(Double feelsLike) { this.feelsLike = feelsLike; }
        
        public Double getMinTemperature() { return minTemperature; }
        public void setMinTemperature(Double minTemperature) { this.minTemperature = minTemperature; }
        
        public Double getMaxTemperature() { return maxTemperature; }
        public void setMaxTemperature(Double maxTemperature) { this.maxTemperature = maxTemperature; }
        
        public Integer getPressure() { return pressure; }
        public void setPressure(Integer pressure) { this.pressure = pressure; }
        
        public Integer getHumidity() { return humidity; }
        public void setHumidity(Integer humidity) { this.humidity = humidity; }
    }

    public static class WeatherCondition {
        @JsonProperty("id")
        private Integer id;
        
        @JsonProperty("main")
        private String main;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("icon")
        private String icon;

        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        
        public String getMain() { return main; }
        public void setMain(String main) { this.main = main; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }

    public static class Wind {
        @JsonProperty("speed")
        private Double speed;
        
        @JsonProperty("deg")
        private Integer direction;

        // Getters and Setters
        public Double getSpeed() { return speed; }
        public void setSpeed(Double speed) { this.speed = speed; }
        
        public Integer getDirection() { return direction; }
        public void setDirection(Integer direction) { this.direction = direction; }
    }

    public static class WeatherForecastResponse {
        @JsonProperty("list")
        private List<ForecastItem> forecast;

        // Getters and Setters
        public List<ForecastItem> getForecast() { return forecast; }
        public void setForecast(List<ForecastItem> forecast) { this.forecast = forecast; }
    }

    public static class ForecastItem {
        @JsonProperty("dt")
        private Long timestamp;
        
        @JsonProperty("main")
        private WeatherMain main;
        
        @JsonProperty("weather")
        private List<WeatherCondition> weather;
        
        @JsonProperty("wind")
        private Wind wind;
        
        @JsonProperty("dt_txt")
        private String dateTime;

        // Getters and Setters
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
        
        public WeatherMain getMain() { return main; }
        public void setMain(WeatherMain main) { this.main = main; }
        
        public List<WeatherCondition> getWeather() { return weather; }
        public void setWeather(List<WeatherCondition> weather) { this.weather = weather; }
        
        public Wind getWind() { return wind; }
        public void setWind(Wind wind) { this.wind = wind; }
        
        public String getDateTime() { return dateTime; }
        public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    }
}
