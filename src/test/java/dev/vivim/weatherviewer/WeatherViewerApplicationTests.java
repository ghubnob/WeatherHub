package dev.vivim.weatherviewer;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
@EnableTransactionManagement
class WeatherViewerApplicationTests {

}
