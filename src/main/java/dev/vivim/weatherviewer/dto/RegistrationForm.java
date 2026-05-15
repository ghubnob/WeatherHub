package dev.vivim.weatherviewer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationForm(@Size(min = 4, max = 20, message = "Имя должно содержать от 4 до 20 символов")
                               @NotBlank(message = "Имя должно содержать от 4 до 20 символов")
                               String login,

                               @Size(min = 8, max = 25, message = "Пароль должен содержать от 8 до 20 символов")
                               @NotBlank(message = "Пароль должен содержать от 8 до 20 символов")
                               String password,

                               String repeatPassword) {
}
