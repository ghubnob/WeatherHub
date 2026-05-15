package dev.vivim.weatherviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WeatherViewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherViewerApplication.class, args);
    }

}
