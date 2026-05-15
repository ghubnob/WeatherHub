package dev.vivim.weatherviewer.service;

import dev.vivim.weatherviewer.model.Session;
import dev.vivim.weatherviewer.model.User;
import dev.vivim.weatherviewer.repository.SessionRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@EnableScheduling
public class SessionsService {
    private final SessionRepository sessionRepository;
    public SessionsService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public void newSession(User user, HttpServletResponse response) {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session(sessionId, user, LocalDateTime.now().plusHours(1));
        sessionRepository.save(session);

        log.info("Creating session with id {}", sessionId);

        Cookie cookie = new Cookie("SESSION_ID", sessionId.toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(3600);
        response.addCookie(cookie);
    }

    public void logoutWithKillSession(String sessionId) {
        log.info("Logging out with session id {}", sessionId);
        sessionRepository.deleteById(UUID.fromString(sessionId));
    }

    public Optional<Session> getValidSession(String sessionIdString) {
        UUID sessionId = UUID.fromString(sessionIdString);
        LocalDateTime now = LocalDateTime.now();
        return sessionRepository.getValidSession(sessionId, now);
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void scheduledRemoveExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        int deleted = sessionRepository.deleteExpiredSessions(now);
        if (deleted > 0) {
            log.info("Deleted {} expired sessions", deleted);
        }
    }
}
