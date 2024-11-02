package com.good.ivrstand.extern.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Конфигурация асинхронности в приложении
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Конфигурирует параметры выполнения асинхронных задач.
     *
     * <p>Параметры:
     * <ul>
     *     <li>Основной пул: 10 потоков</li>
     *     <li>Максимальный пул: 50 потоков</li>
     *     <li>Емкость очереди: 100 задач</li>
     * </ul>
     */
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.initialize();
        return executor;
    }
}
