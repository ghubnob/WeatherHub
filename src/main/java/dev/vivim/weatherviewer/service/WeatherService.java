package dev.vivim.weatherviewer.service;

import dev.vivim.weatherviewer.dto.WeatherLocationDTO;
import dev.vivim.weatherviewer.dto.WeatherLocationRequest;
import dev.vivim.weatherviewer.dto.WeatherProperties;
import dev.vivim.weatherviewer.dto.weatherapiresponse.OpenWeatherResponse;
import dev.vivim.weatherviewer.dto.weatherapiresponse.OpenWeatherSearchResponse;
import dev.vivim.weatherviewer.model.Location;
import dev.vivim.weatherviewer.model.User;
import dev.vivim.weatherviewer.repository.LocationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class WeatherService {
    private final LocationRepository locationRepository;
    private final RestTemplate restTemplate;
    private final WeatherProperties weatherProperties;

    private record CacheEntry(WeatherLocationDTO dto, long expiresAt) {}
    private final Map<Integer, CacheEntry> cache;

    private static final double EPSILON = 1e-6;

    public WeatherService(LocationRepository locationRepository, RestTemplate restTemplate, WeatherProperties weatherProperties) {
        this.locationRepository = locationRepository;
        this.restTemplate = restTemplate;
        this.weatherProperties = weatherProperties;

        cache = new ConcurrentHashMap<>();
    }

    private OpenWeatherResponse getWeather(double lon, double lat) {
        return restTemplate.getForObject(weatherProperties.getApiUrlCity(), OpenWeatherResponse.class, weatherProperties.getApiKey(), lon, lat);
    }

    /**
     * GET method - получаем объекты погоды
     */
    public List<OpenWeatherSearchResponse> getListSearchLocations(String inputCity) {
        OpenWeatherSearchResponse[] responses = restTemplate.getForObject(weatherProperties.getApiUrlCities(), OpenWeatherSearchResponse[].class, inputCity, weatherProperties.getApiKey());
        if (responses == null || responses.length == 0) return List.of();
        log.info("Found {} cities with name {}", responses.length, inputCity);

        return List.of(responses);
    }

    /**
     * POST method
     * @param request DTO со всеми данными необходимыми локации
     */
    public void addNewLocation(WeatherLocationRequest request, User user) {
        Location location = new Location(request.locationName(), user, request.latitude(), request.longitude());
        for (Location loc : locationRepository.findUserLocations(user.getId())) {
            // Не допускаем дубликаты локаций - сравниваем через эпсилон (double типы плохо сравниваются а id получить пока не можем)
            if (Math.abs(loc.getLatitude() - location.getLatitude()) < EPSILON
                    && Math.abs(loc.getLongitude() - location.getLongitude()) < EPSILON) {
                return;
            }
        }

        locationRepository.save(location);
    }

    /**
     * POST method удаления
     */
    public void removeLocation(int id, User user) {
        log.info("Removing locations with id {} exists: {}",id,locationRepository.existsById(id));
        locationRepository.deleteLocation(id, user.getId());
    }

    public List<WeatherLocationDTO> getUserWeathers(User user) {
        return parseLocations(locationRepository.findUserLocations(user.getId()));
    }

    private List<WeatherLocationDTO> parseLocations(List<Location> locationsDB) {
        List<WeatherLocationDTO> result = new ArrayList<>();
        for (Location location : locationsDB) {
            if (cache.containsKey(location.getId())) {
                result.add(cache.get(location.getId()).dto());
                continue;
            }
            OpenWeatherResponse owr = getWeather(location.getLongitude(), location.getLatitude());
            WeatherLocationDTO dto = new WeatherLocationDTO(
                    location.getId(),
                    owr.name(),
                    owr.sys().country(),
                    owr.main().temp(),
                    owr.main().feels_like(),
                    owr.main().humidity(),
                    owr.weather().getFirst().icon(),
                    owr.weather().getFirst().description(),
                    owr.coord().lat(),
                    owr.coord().lon());
            result.add(dto);
            cache.put(location.getId(), new CacheEntry(dto, System.currentTimeMillis() + 1000*60*10));
        }
        return result;
    }

    @Scheduled(fixedRate = 300000)
    private void cleanCache() {
        cache.values().removeIf(entry -> entry.expiresAt() < System.currentTimeMillis());
    }
}
