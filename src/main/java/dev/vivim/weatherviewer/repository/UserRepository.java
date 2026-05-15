package dev.vivim.weatherviewer.repository;

import dev.vivim.weatherviewer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByLogin(String username);
}
