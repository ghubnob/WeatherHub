package dev.vivim.weatherviewer;

import dev.vivim.weatherviewer.model.User;
import dev.vivim.weatherviewer.repository.UserRepository;
import dev.vivim.weatherviewer.service.SessionsService;
import dev.vivim.weatherviewer.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsersTests {
    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    @Test
    void shouldSaveUserWhenRegistrationWithValidData() {
        String username = "username";
        when(userRepository.findByLogin(any())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Assertions.assertDoesNotThrow(() ->
                userService.registerNewUser("username", "password", "password"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        Assertions.assertNotNull(captor.getValue());
        Assertions.assertEquals(username, captor.getValue().getLogin());
    }

}
