package dev.vivim.weatherviewer.service;

import dev.vivim.weatherviewer.dto.WeatherLocationDTO;
import dev.vivim.weatherviewer.dto.WeatherLocationRequest;
import dev.vivim.weatherviewer.dto.weatherapiresponse.OpenWeatherResponse;
import dev.vivim.weatherviewer.dto.weatherapiresponse.OpenWeatherSearchResponse;
import dev.vivim.weatherviewer.model.Location;
import dev.vivim.weatherviewer.model.User;
import dev.vivim.weatherviewer.repository.LocationRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WeatherService {
    private final LocationRepository locationRepository;

    @Value("${openweather.api.key}")
    private String API_KEY;
    private static final String API_URL_GETFROMLONLAT = "https://api.openweathermap.org/data/2.5/weather?appid={API_KEY}&lon={lon}&lat={lat}&units=metric&lang=ru";
    private static final String API_URL_GETCITIES = "http://api.openweathermap.org/geo/1.0/direct?q={cityName}&limit=8&appid={API_KEY}&lang=ru";

    private static final double EPSILON = 1e-6;

    public WeatherService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    private OpenWeatherResponse getWeather(double lon, double lat) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(API_URL_GETFROMLONLAT, OpenWeatherResponse.class, API_KEY, lon, lat);
    }

    /**
     * GET method - получаем объекты погоды и кладем их в модель
     */
    public String getWeathersSearch(String inputCity, HttpServletRequest request, Model model) {
        User user = (User) request.getAttribute("user");
        if (user == null) return "redirect:/sign-in";
        model.addAttribute("username", user.getLogin());
        log.info("User {} finding locations", user.getLogin());

        RestTemplate restTemplate = new RestTemplate();

        OpenWeatherSearchResponse[] responses = restTemplate.getForObject(API_URL_GETCITIES, OpenWeatherSearchResponse[].class, inputCity, API_KEY);
        if (responses == null || responses.length == 0) return "/search-results";
        log.info("Found {} cities with name {}", responses.length, inputCity);

        List<OpenWeatherSearchResponse> response = List.of(responses);
        model.addAttribute("response", response);

        return "/search-results";
    }

    /**
     * POST method
     * @param request DTO со всеми данными необходимыми локации
     * @param httpRequest HttpServletRequest из которого возьмем сессию пользователя (либо вернем его на страницу авторизации)
     */
    public String addNewLocation(WeatherLocationRequest request, HttpServletRequest httpRequest) {
        User user = (User) httpRequest.getAttribute("user");
        if (user == null) return "redirect:/sign-in";

        Location location = new Location(null, request.locationName(), user, request.latitude(), request.longitude());
        for (Location loc : locationRepository.findUserLocations(user.getId())) {
            // Не допускаем дубликаты локаций - сравниваем через эпсилон (double типы плохо сравниваются а id получить пока не можем)
            if (Math.abs(loc.getLatitude() - location.getLatitude()) < EPSILON
                    && Math.abs(loc.getLongitude() - location.getLongitude()) < EPSILON) {
                return "redirect:/";
            }
        }

        locationRepository.save(location);
        return "redirect:/";
    }

    /**
     * POST method удаления
     * @param httpRequest HttpServletRequest из которого возьмем сессию пользователя (либо вернем его на страницу авторизации)
     */
    public String removeLocation(int id, HttpServletRequest httpRequest) {
        User user = (User) httpRequest.getAttribute("user");
        if (user == null) return "redirect:/sign-in";

        log.info("Removing locations with id {} exists: {}",id,locationRepository.existsById(id));
        locationRepository.deleteById(id);
        return "redirect:/";
    }

    public String getUserWeathers(HttpServletRequest httpRequest, Model model) {
        User user = (User) httpRequest.getAttribute("user");
        if (user == null) return "redirect:/sign-in";

        List<WeatherLocationDTO> locations = parseLocations(locationRepository.findUserLocations(user.getId()));
        model.addAttribute("locations", locations);
        return "/index";
    }

    private List<WeatherLocationDTO> parseLocations(List<Location> locationsDB) {
        List<WeatherLocationDTO> result = new ArrayList<>();
        for (Location location : locationsDB) {
            OpenWeatherResponse owr = getWeather(location.getLongitude(), location.getLatitude());
            result.add(new WeatherLocationDTO(
                    location.getId(),
                    owr.name(),
                    owr.sys().country(),
                    owr.main().temp(),
                    owr.main().feels_like(),
                    owr.main().humidity(),
                    owr.weather().getFirst().icon(),
                    owr.weather().getFirst().description(),
                    owr.coord().lat(),
                    owr.coord().lon()
            ));
        }
        return result;
    }
}
