package com.good.ivrstand.app;

import com.good.ivrstand.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@Slf4j
public class TokenService {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 32;

    private ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    private final TaskScheduler taskScheduler;
    private final UserRepository userRepository;

    public TokenService(TaskScheduler taskScheduler, UserRepository userRepository) {
        this.taskScheduler = taskScheduler;
        this.userRepository = userRepository;
    }

    public String generateResetPasswordToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(LENGTH);

        for (int i = 0; i < LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        String randomString = sb.toString();
        return randomString;
    }

    @Async
    public void scheduleTokenInvalidation(long userId, long delayInMilliseconds) {
        Instant startTime = Instant.now().plusMillis(delayInMilliseconds);
        User user = userRepository.findById(userId);

        if (user == null)
            throw new IllegalArgumentException("Пользователь не может быть null");

        ScheduledFuture<?> existingTask = scheduledTasks.get(userId);
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(true);
            scheduledTasks.remove(userId);
        }

        ScheduledFuture<?> newTask = taskScheduler.schedule(() -> invalidateToken(userId), startTime);
        scheduledTasks.put(userId, newTask);
        log.info("Задан тайм-аут токена сброса пароля для пользователя {}", userId);

    }

    public void invalidateToken(long userId) {
        User user = userRepository.findById(userId);

        if (user == null)
            throw new IllegalArgumentException("Пользователь не может быть null");

        user.setResetToken("no-token");
        ScheduledFuture<?> existingTask = scheduledTasks.get(userId);
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(true);
            scheduledTasks.remove(userId);
        }
        log.info("Истёк токен сброса пароля для пользователя {}", userId);
        userRepository.save(user);
    }
}
