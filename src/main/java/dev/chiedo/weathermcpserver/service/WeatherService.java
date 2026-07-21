package dev.chiedo.weathermcpserver.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import dev.chiedo.weathermcpserver.model.WeatherDataResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

@Service
public class WeatherService {

    private final static Logger LOGGER = LoggerFactory.getLogger(WeatherService.class);
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public WeatherService(RestClient restClient, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherDescription(
            @JsonProperty("value") String value
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrentWeatherCondition(
            @JsonProperty("temp_C") String tempCelsius,
            @JsonProperty("temp_F") String tempFahrenheit,
            @JsonProperty("humidity") String humidity,
            @JsonProperty("windspeedKmph") String windSpeed,
            @JsonProperty("weatherDesc") List<WeatherDescription> weatherDescription
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WeatherApiResponse(
            @JsonProperty("current_condition")
            List<CurrentWeatherCondition> currentWeatherCondition
    ) {}

    @Tool(description = "Get weather details for a specific location")
    public WeatherDataResponse getWeatherDetailsByLocation(String location) {
        String encodedLocation = UriUtils.encode(location, StandardCharsets.UTF_8);
        String url = encodedLocation + "?format=j1";

        String json = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        WeatherApiResponse apiResponse = objectMapper.readValue(json, WeatherApiResponse.class);

        CurrentWeatherCondition currentWeatherCondition = apiResponse.currentWeatherCondition().get(0);
        String weatherDescription = currentWeatherCondition.weatherDescription().get(0).value();

        LOGGER.debug("Log request for location {}", location);
        LOGGER.debug("Weather conditions: {}", Objects.toString(apiResponse));

        return new WeatherDataResponse(
                location,
                weatherDescription,
                currentWeatherCondition.tempCelsius(),
                currentWeatherCondition.tempFahrenheit(),
                currentWeatherCondition.humidity(),
                currentWeatherCondition.windSpeed()
        );
    }
}
