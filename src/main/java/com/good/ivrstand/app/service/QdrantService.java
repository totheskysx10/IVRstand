package com.good.ivrstand.app.service;

import com.good.ivrstand.exception.ItemsFindException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * Сервис для раобты с базой Qdrant
 */
@Component
@Slf4j
public class QdrantService {

    private final FlaskApiVectorSearchService flaskApiVectorSearchService;

    public QdrantService(FlaskApiVectorSearchService flaskApiVectorSearchService) {
        this.flaskApiVectorSearchService = flaskApiVectorSearchService;
    }

    /**
     * Синхронизирует базу данных Qdrant с PostgreSQL асинхронно с возможностью частичного ожидания.
     *
     * <p>Метод выполняет синхронизацию базы данных асинхронно, но ожидает завершения синхронизации
     * в течение заданного таймаута (3000 мс). Если синхронизация не завершилась за это время,
     * выбрасывается исключение с сообщением о продолжающемся процессе.</p>
     *
     * <p>Исключения, возникшие в процессе выполнения асинхронной задачи, корректно обрабатываются и
     * конвертируются в {@link ItemsFindException}.</p>
     *
     * @throws ItemsFindException если:
     *                            <ul>
     *                              <li>База данных уже синхронизируется (передано из Feign-клиента).</li>
     *                              <li>Синхронизация заняла больше 3000 мс.</li>
     *                            </ul>
     * @throws RuntimeException если выполнение было прервано
     */
    public void syncDatabase() throws ItemsFindException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                flaskApiVectorSearchService.syncDatabase();
            } catch (ItemsFindException e) {
                throw new CompletionException(e);
            }
        });

        try {
            future.get(3000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw new ItemsFindException(cause.getMessage());
        } catch (TimeoutException e) {
            throw new ItemsFindException("Идёт синхронизация БД, ожидайте 5-7 минут");
        } catch (InterruptedException e) {
            throw new RuntimeException("Error during sync", e);
        }
    }
}
