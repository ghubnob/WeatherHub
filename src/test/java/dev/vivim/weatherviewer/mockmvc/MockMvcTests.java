package dev.vivim.weatherviewer.mockmvc;

import dev.vivim.weatherviewer.MainController;
import dev.vivim.weatherviewer.dto.RegistrationForm;
import dev.vivim.weatherviewer.exceptions.PasswordsNotMatchException;
import dev.vivim.weatherviewer.exceptions.UserExistsException;
import dev.vivim.weatherviewer.model.User;
import dev.vivim.weatherviewer.service.SessionsService;
import dev.vivim.weatherviewer.service.UserService;
import dev.vivim.weatherviewer.service.WeatherService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
public class MockMvcTests {
    @Autowired MockMvc mockMvc;
    @MockitoBean UserService userService;
    @MockitoBean SessionsService sessionsService;
    @MockitoBean WeatherService weatherService;

    @Test
    void shouldRedirectIfCookieNotFound() throws Exception {
        mockMvc.perform(get("/")
                .cookie(new Cookie("SESSION_ID", "uuid")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"));
    }

    @Test
    void shouldRedirectToIndexPageWhenCreateGoodUser() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("login", "username")
                        .param("password", "password")
                        .param("repeatPassword", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void shouldGetSessionWhenSignUp() throws Exception {
        String knownId = UUID.randomUUID().toString();
        when(userService.registerNewUser(eq("username"), eq("password"), eq("password")))
                .thenReturn(new User("username","password"));
        when(sessionsService.newSession(any())).thenReturn(knownId);

        MvcResult res = mockMvc.perform(post("/sign-up")
                .param("login", "username")
                .param("password", "password")
                .param("repeatPassword", "password"))
            .andExpect(status().is3xxRedirection())
            .andReturn();

        Cookie cookie = res.getResponse().getCookie("SESSION_ID");
        Assertions.assertNotNull(cookie);
        Assertions.assertEquals(knownId, cookie.getValue());
        Assertions.assertEquals("/", cookie.getPath());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        Assertions.assertEquals(knownId, verify(sessionsService).newSession(captor.capture()));
        Assertions.assertEquals("username", captor.getValue().getLogin());
        Assertions.assertEquals("password", captor.getValue().getPassword());
    }
}
