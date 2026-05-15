package dev.vivim.weatherviewer;

import dev.vivim.weatherviewer.dto.AuthenticationForm;
import dev.vivim.weatherviewer.dto.RegistrationForm;
import dev.vivim.weatherviewer.dto.WeatherLocationRequest;
import dev.vivim.weatherviewer.exceptions.FailedAuthenticationException;
import dev.vivim.weatherviewer.exceptions.PasswordsNotMatchException;
import dev.vivim.weatherviewer.exceptions.UserExistsException;
import dev.vivim.weatherviewer.exceptions.UserNotFoundException;
import dev.vivim.weatherviewer.model.User;
import dev.vivim.weatherviewer.service.SessionsService;
import dev.vivim.weatherviewer.service.UserService;
import dev.vivim.weatherviewer.service.WeatherService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@Controller
public class MainController {
    private final UserService userService;
    private final SessionsService sessionsService;
    private final WeatherService weatherService;
    public MainController(UserService userService, SessionsService sessionsService, WeatherService weatherService) {
        this.userService = userService;
        this.sessionsService = sessionsService;
        this.weatherService = weatherService;
    }

    @GetMapping("/")
    public String getMainPage(HttpServletRequest request, Model model) {
        Optional<User> userOpt = Optional.ofNullable((User) request.getAttribute("user"));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            log.info("On main page, username={}", user.getLogin());
            model.addAttribute("username", user.getLogin());

            model.addAttribute("locations",weatherService.getUserWeathers(user));
            return "/index";
        }
        else {
            log.info("On main page, user is null!");
            return "redirect:/sign-in";
        }
    }


    @GetMapping("/sign-up")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationForm(null, null, null));
        return "/sign-up";
    }

    @PostMapping("/sign-up")
    public String registerUser(@Valid @ModelAttribute("user") RegistrationForm form,
                               BindingResult bindingResult,
                               HttpServletResponse response,
                               Model model) {
        log.info("Sign up page clicked: {}", form);
        if (bindingResult.hasErrors()) {
            return "/sign-up";
        }
        try {
            log.info("Trying to register new user: {}", form.login());
            User user = userService.registerNewUser(form.login(), form.password(), form.repeatPassword());
            sessionsService.newSession(user, response);
            return "redirect:/";
        }
        catch (UserExistsException | PasswordsNotMatchException e) {
            model.addAttribute("error", e);
            return "/sign-up";
        }
    }



    @GetMapping("/sign-in")
    public String showLoginForm(Model model) {
        model.addAttribute("loginForm", new AuthenticationForm("",""));
        return "/sign-in";
    }

    @PostMapping("/sign-in")
    public String loginUser(@Valid @ModelAttribute("loginForm") AuthenticationForm form,
                            BindingResult bindingResult,
                            HttpServletResponse response,
                            Model model) {
        log.info("Trying to log in: {}", form);
        if (bindingResult.hasErrors()) return "/sign-in";
        try {
            log.info("Logging in user: {}", form.username());
            User user = userService.authenticate(form.username(), form.password());
            sessionsService.newSession(user, response);
            return "redirect:/";
        } catch (UserNotFoundException | FailedAuthenticationException e) {
            model.addAttribute("error", e);
            return "/sign-in";
        }
    }


    @GetMapping("/logout")
    public String logout(@CookieValue(name = "SESSION_ID", required = false) String sessionId, HttpServletResponse response) {
        if (sessionId != null) {
            sessionsService.logoutWithKillSession(sessionId);

            Cookie cookie = new Cookie("SESSION_ID", "");
            cookie.setMaxAge(0);
            cookie.setPath("/");

            response.addCookie(cookie);
        }
        return "redirect:/sign-in";
    }


    @PostMapping("/locations")
    public String searchWeather(@RequestParam String name, HttpServletRequest request, Model model) {
        Optional<User> userOpt = Optional.ofNullable((User) request.getAttribute("user"));
        if (userOpt.isEmpty()) return "redirect:/sign-in";

        model.addAttribute("username", userOpt.get().getLogin());
        log.info("User {} finding locations", userOpt.get().getLogin());

        model.addAttribute("response", weatherService.getWeathersSearch(name));
        return "/search-results";
    }

    @PostMapping("/locations/add")
    public String addLocation(@ModelAttribute WeatherLocationRequest request, HttpServletRequest httpRequest) {
        Optional<User> userOpt = Optional.ofNullable((User) httpRequest.getAttribute("user"));
        if (userOpt.isEmpty()) return "redirect:/sign-in";

        User user = userOpt.get();

        weatherService.addNewLocation(request, user);
        return "redirect:/";
    }

    @PostMapping("/locations/delete/{id}")
    public String removeLocation(@PathVariable int id, HttpServletRequest httpRequest) {
        Optional<User> userOpt = Optional.ofNullable((User) httpRequest.getAttribute("user"));
        if (userOpt.isEmpty()) return "redirect:/sign-in";

        User user = userOpt.get();
        weatherService.removeLocation(id, user);
        return "redirect:/";
    }
}
