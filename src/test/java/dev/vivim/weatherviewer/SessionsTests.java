package dev.vivim.weatherviewer;

import dev.vivim.weatherviewer.repository.SessionRepository;
import dev.vivim.weatherviewer.service.SessionsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionsTests {
    @Mock SessionRepository sessionRepository;
    @InjectMocks SessionsService sessionsService;

    @Test
    void shouldKillSessionAndRedirectWhenLogoutUser() {
        sessionsService.logoutWithKillSession(UUID.randomUUID().toString());

        verify(sessionRepository, times(1)).deleteById(any());
    }
}
