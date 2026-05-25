package dev.vivim.weatherviewer.integration;

import dev.vivim.weatherviewer.exceptions.PasswordsNotMatchException;
import dev.vivim.weatherviewer.exceptions.UserExistsException;
import dev.vivim.weatherviewer.model.User;
import dev.vivim.weatherviewer.repository.LocationRepository;
import dev.vivim.weatherviewer.repository.SessionRepository;
import dev.vivim.weatherviewer.repository.UserRepository;
import dev.vivim.weatherviewer.service.SessionsService;
import dev.vivim.weatherviewer.service.UserService;
import dev.vivim.weatherviewer.service.WeatherService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
public class IntegrationTests {
    @Autowired UserService userService;
    @Autowired UserRepository userRepository;

    @Autowired WeatherService weatherService;
    @Autowired LocationRepository locationRepository;

    @Autowired SessionsService sessionsService;
    @Autowired SessionRepository sessionRepository;

    @Test
    void shouldRegister() {
        String username = "username";
        String password1 = "password";
        String password2 = "password";

        User user = userService.registerNewUser(username, password1, password2);
        Assertions.assertNotNull(user);
        Assertions.assertEquals(username, user.getLogin());
    }

    @Test
    void shouldErrorWhenRegisterAndDiffPasses() {
        String username = "username";
        String password1 = "password";
        String password2 = "password1";

        Assertions.assertThrows(PasswordsNotMatchException.class, () -> userService.registerNewUser(username, password1, password2));
    }

    @Test
    void shouldErrorWhenRegisterAndUserExists() {
        String username = "username";
        String password1 = "password";
        String password2 = "password";

        userService.registerNewUser(username, password1, password2);

        Assertions.assertThrows(UserExistsException.class, () ->
                userService.registerNewUser(username, password1, password2));
    }
}
