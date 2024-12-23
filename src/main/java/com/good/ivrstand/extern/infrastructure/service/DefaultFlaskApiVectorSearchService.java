package com.good.ivrstand.extern.infrastructure.service;

import com.good.ivrstand.app.service.externinterfaces.FlaskApiVectorSearchService;
import com.good.ivrstand.exception.ItemsFindException;
import com.good.ivrstand.extern.api.flaskRequests.AddTitleRequest;
import com.good.ivrstand.extern.api.flaskRequests.TitleRequest;
import com.good.ivrstand.extern.infrastructure.clients.FlaskApiVectorSearchClient;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Сервис векторного поиска
 */
@Component
public class DefaultFlaskApiVectorSearchService implements FlaskApiVectorSearchService {

    private final FlaskApiVectorSearchClient flaskApiVectorSearchClient;

    public DefaultFlaskApiVectorSearchService(FlaskApiVectorSearchClient flaskApiVectorSearchClient) {
        this.flaskApiVectorSearchClient = flaskApiVectorSearchClient;
    }

    /**
     * Вызывает в Feign-клиенте метод получения списка с Id найденных услуг.
     *
     * @param request запрос
     */
    public List<Long> getItemIds(String request) {
        Map<String, String> requestData = new HashMap<>();
        requestData.put("request", request);

        return flaskApiVectorSearchClient.getItemIds(requestData);
    }

    /**
     * Вызывает в Feign-клиенте метод добавления услуги в базу Qdrant.
     *
     * @param addTitleRequest запрос
     */
    public void addTitle(AddTitleRequest addTitleRequest) {
        flaskApiVectorSearchClient.addTitle(addTitleRequest);
    }

    /**
     * Вызывает в Feign-клиенте метод удаления услуги из базы Qdrant.
     *
     * @param request запрос
     */
    public void deleteTitle(TitleRequest request) {
        flaskApiVectorSearchClient.deleteTitle(request);
    }

    /**
     * Выполняет синхронизацию базы Qdrant с PostgreSQL асинхронно с возможностью частичного ожидания.
     *
     * @throws ItemsFindException если:
     *                            <ul>
     *                              <li>База данных уже синхронизируется (HTTP 429).</li>
     *                              <li>Синхронизация заняла больше 3000 мс.</li>
     *                            </ul>
     */
    public void syncDatabase() throws ItemsFindException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                flaskApiVectorSearchClient.syncDatabase();
            } catch (FeignException.TooManyRequests e) {
                throw new RuntimeException("Too many requests error");
            }
        });

        try {
            future.get(3000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw new ItemsFindException("БД уже в процессе синхронизации, ожидайте 5-7 минут");
        } catch (TimeoutException e) {
            throw new ItemsFindException("Идёт синхронизация БД, ожидайте 5-7 минут");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ItemsFindException("Синхронизация была прервана");
        }
    }
}
