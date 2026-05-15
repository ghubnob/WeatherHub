package dev.vivim.weatherviewer.dto.weatherapiresponse;

public record OpenWeatherSearchResponse(String name, double lat, double lon, String country, String state) {}
