package dev.vivim.weatherviewer.service;

import dev.vivim.weatherviewer.repository.UserRepository;
import dev.vivim.weatherviewer.exceptions.FailedAuthenticationException;
import dev.vivim.weatherviewer.exceptions.PasswordsNotMatchException;
import dev.vivim.weatherviewer.exceptions.UserExistsException;
import dev.vivim.weatherviewer.exceptions.UserNotFoundException;
import dev.vivim.weatherviewer.model.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Optional;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final WeatherService weatherService;
    public UserService(UserRepository userRepository, WeatherService weatherService) {
        this.userRepository = userRepository;
        this.weatherService = weatherService;
    }

    /**
     * POST - регистрация
     * @return UUID новой активной сессии
     */
    public User registerNewUser(String username, String password, String repeatPassword) {
        if (!password.equals(repeatPassword)) throw new PasswordsNotMatchException("Passwords do not match");

        Optional<User> user = userRepository.findByLogin(username);
        if (user.isPresent()) throw new UserExistsException("User already exists");

        User newUser = new User(null, username, password);
        userRepository.save(newUser);
        return newUser;
    }

    /**
     * GET method - отображение погоды пользователя или авторизация
     */
    public String onMainPage(HttpServletRequest request, Model model) {
        User user = (User) request.getAttribute("user");
        if (user != null) {
            log.info("On main page, username={}", user.getLogin());
            model.addAttribute("username", user.getLogin());
            return weatherService.getUserWeathers(request, model);
        }
        else {
            log.info("On main page, user is null!");
            return "redirect:/sign-in";
        }
    }

    /**
     * POST - авторизация
     * @return UUID новой активной сессии
     */
    public User authenticate(String username, String password) {
        log.debug("Authenticating user {}", username);
        User user = userRepository.findByLogin(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!user.getPassword().equals(password)) throw new FailedAuthenticationException("Wrong password");

        return user;
    }
}
