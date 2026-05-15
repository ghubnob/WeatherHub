package dev.vivim.weatherviewer.web;

import dev.vivim.weatherviewer.model.Session;
import dev.vivim.weatherviewer.service.SessionsService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final SessionsService sessionsService;
    public AuthInterceptor(SessionsService sessionsService) {
        this.sessionsService = sessionsService;
    }

    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (request.getCookies() == null) {
            response.sendRedirect("/sign-in");
            return false;
        }

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("SESSION_ID")) {
                String sessionId = cookie.getValue();
                Optional<Session> sessionOpt = sessionsService.getValidSession(sessionId);

                if (sessionOpt.isPresent()) {
                    request.setAttribute("user", sessionOpt.get().getUser());
                    return true;
                }
                else {
                    response.sendRedirect("/sign-in");
                    return false;
                }
            }
        }
        response.sendRedirect("/sign-in");
        return false;
    }
}
