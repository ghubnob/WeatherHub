package dev.vivim.weatherviewer.dto.weatherapiresponse;

import java.util.List;

public record OpenWeatherResponse(
        Coord coord,
        List<Weather> weather,
        String base,
        Main main,
        Long visibility,
        Wind wind,
        Clouds clouds,
        Long dt,
        Sys sys,
        Integer timezone,
        Long id,
        String name,
        Integer cod) {}