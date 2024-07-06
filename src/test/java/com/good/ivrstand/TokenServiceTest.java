package com.good.ivrstand;

import com.good.ivrstand.app.TokenService;
import com.good.ivrstand.app.UserRepository;
import com.good.ivrstand.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ScheduledFuture<?> scheduledFuture;

    @InjectMocks
    private TokenService tokenService;


    @Test
    public void testGenerateResetPasswordToken() {
        String token = tokenService.generateResetPasswordToken();
        assertNotNull(token);
        assertEquals(32, token.length());
    }

    @Test
    public void testScheduleTokenInvalidation() {
        User user = User.builder()
                .id(23L)
                .email("min@list.ru")
                .username("test")
                .password("test")
                .resetToken("token")
                .build();

        when(userRepository.findById(1L)).thenReturn(user);
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenAnswer(unused -> scheduledFuture);

        tokenService.scheduleTokenInvalidation(1L, 10000);

        verify(taskScheduler, times(1)).schedule(any(Runnable.class), any(Instant.class));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testInvalidateToken() {
        User user = User.builder()
                .id(23L)
                .email("min@list.ru")
                .username("test")
                .password("test")
                .resetToken("token")
                .build();

        when(userRepository.findById(1L)).thenReturn(user);

        tokenService.invalidateToken(1L);

        assertEquals("no-token", user.getResetToken());
        verify(userRepository, times(1)).save(user);
    }
}
