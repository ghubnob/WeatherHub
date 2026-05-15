package dev.vivim.weatherviewer.service;

import dev.vivim.weatherviewer.repository.UserRepository;
import dev.vivim.weatherviewer.exceptions.FailedAuthenticationException;
import dev.vivim.weatherviewer.exceptions.PasswordsNotMatchException;
import dev.vivim.weatherviewer.exceptions.UserExistsException;
import dev.vivim.weatherviewer.exceptions.UserNotFoundException;
import dev.vivim.weatherviewer.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
