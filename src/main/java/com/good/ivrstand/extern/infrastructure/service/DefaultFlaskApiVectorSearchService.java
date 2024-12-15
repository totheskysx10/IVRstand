package com.good.ivrstand.extern.infrastructure.service;

import com.good.ivrstand.app.service.FlaskApiVectorSearchService;
import com.good.ivrstand.exception.ItemsFindException;
import com.good.ivrstand.extern.api.flaskRequests.AddTitleRequest;
import com.good.ivrstand.extern.api.flaskRequests.TitleRequest;
import com.good.ivrstand.extern.infrastructure.clients.FlaskApiVectorSearchClient;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Вызывает метод синхронизации базы Qdrant с базой PostgreSQL через Feign-клиент.
     *
     * Если база данных уже в процессе синхронизации (HTTP код 429 - Too Many Requests),
     * выбрасывает исключение {@link ItemsFindException} с соответствующим сообщением.
     *
     * @throws ItemsFindException если база данных уже синхронизируется
     */
    public void syncDatabase() throws ItemsFindException {
        try {
            flaskApiVectorSearchClient.syncDatabase();
        } catch (FeignException.TooManyRequests e) {
            throw new ItemsFindException("БД уже в процессе синхронизации, ожидайте 5-7 минут");
        }
    }
}
