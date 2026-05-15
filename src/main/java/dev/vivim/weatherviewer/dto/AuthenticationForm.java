package dev.vivim.weatherviewer.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationForm(@NotBlank(message = "Имя пользователя не может быть пустым") String username,
                                 @NotBlank(message = "Имя пользователя не может быть пустым") String password) {
}
