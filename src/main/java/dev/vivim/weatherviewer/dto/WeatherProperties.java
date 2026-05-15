package dev.vivim.weatherviewer.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "openweather")
@Data
public class WeatherProperties {
    private String apiKey;
    private String apiUrlCity;
    private String apiUrlCities;
}
