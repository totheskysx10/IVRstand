package com.good.ivrstand.extern.infrastructure;

import com.good.ivrstand.app.FlaskApiVectorSearchService;
import com.good.ivrstand.extern.api.flaskRequests.AddTitleRequest;
import com.good.ivrstand.extern.api.flaskRequests.TitleRequest;
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
     * Вызывает в Feign-клиенте метод синхронизации базы Qdrant с базой PostreSQL.
     */
    public void syncDatabase() {
        flaskApiVectorSearchClient.syncDatabase();
    }
}
