package dev.vivim.weatherviewer.dto;

public record WeatherLocationDTO(long id,
                                 String cityName,
                                 String countryCode,
                                 double temperature,
                                 double feelsLike,
                                 int humidity,
                                 String weatherIcon,
                                 String description,
                                 double lat,
                                 double lon) {}