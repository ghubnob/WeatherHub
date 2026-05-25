package dev.vivim.weatherviewer.apitests;

import dev.vivim.weatherviewer.dto.weatherapiresponse.OpenWeatherSearchResponse;
import dev.vivim.weatherviewer.service.WeatherService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OpenWeatherApiTests {
    @MockitoBean private RestTemplate restTemplate;
    @Autowired private WeatherService weatherService;

    @Test
    void shouldReturnCitiesWeather() {
        OpenWeatherSearchResponse[] ob = { new OpenWeatherSearchResponse("123", 1, 2, "IO", "CL") };
        when(restTemplate.getForObject(anyString(), eq(OpenWeatherSearchResponse[].class), anyString(), anyString()))
                .thenReturn(ob);

        List<OpenWeatherSearchResponse> response = weatherService.getListSearchLocations("London");
        verify(restTemplate, times(1)).getForObject(anyString(), eq(OpenWeatherSearchResponse[].class), anyString(), anyString());
        Assertions.assertFalse(response.isEmpty());
    }
}
