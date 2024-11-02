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

/**
 * Сервис для работы с токенам сброса пароля
 */
@Component
@Slf4j
public class TokenService {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 32;

    /**
     * Значение токена сброса пароля, когда он не задан
     */
    private static final String NO_TOKEN = "no-token";

    /**
     * Запланированные задачи на истечение токенов сброса пароля.
     */
    private ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    private final TaskScheduler taskScheduler;
    private final UserRepository userRepository;

    public TokenService(TaskScheduler taskScheduler, UserRepository userRepository) {
        this.taskScheduler = taskScheduler;
        this.userRepository = userRepository;
    }

    /**
     * Генерирует токен сброса пароля
     */
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

    /**
     * Задаёт пользователю токен сброса пароля, создаёт задачу на его истечение.
     * @param userId id пользователя
     * @param delayInMilliseconds время действия токена
     */
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

    /**
     * Делает токен сброса пароля недействительным.
     * Удаляет действующий токен из базы и заменяет на "no-token"
     * @param userId id пользователя
     */
    public void invalidateToken(long userId) {
        User user = userRepository.findById(userId);

        if (user == null)
            throw new IllegalArgumentException("Пользователь не может быть null");

        user.setResetToken(NO_TOKEN);
        ScheduledFuture<?> existingTask = scheduledTasks.get(userId);
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(true);
            scheduledTasks.remove(userId);
        }
        log.info("Истёк токен сброса пароля для пользователя {}", userId);
        userRepository.save(user);
    }
}
