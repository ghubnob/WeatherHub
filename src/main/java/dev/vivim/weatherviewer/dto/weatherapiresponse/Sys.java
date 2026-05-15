package dev.vivim.weatherviewer.dto.weatherapiresponse;

public record Sys(
        Integer type,
        Long id,
        String country,
        Long sunrise,
        Long sunset) {}