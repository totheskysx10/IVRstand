package com.good.ivrstand.extern.infrastructure;

import com.good.ivrstand.app.FlaskApiVectorSearchService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DefaultFlaskApiVectorSearchService implements FlaskApiVectorSearchService {

    private final FlaskApiVectorSearchClient flaskApiVectorSearchClient;

    public DefaultFlaskApiVectorSearchService(FlaskApiVectorSearchClient flaskApiVectorSearchClient) {
        this.flaskApiVectorSearchClient = flaskApiVectorSearchClient;
    }

    public List<String> getEmbeddings(List<String> dialog) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("dialog", dialog);

        return flaskApiVectorSearchClient.getEmbeddings(requestData);
    }

    public void addTitle(String title) {
        String requestBody = "{\"text\": \"" + title + "\"}";
        flaskApiVectorSearchClient.addTitle(requestBody);
    }

    public void deleteTitle(String title) {
        String requestBody = "{\"text\": \"" + title + "\"}";
        flaskApiVectorSearchClient.deleteTitle(requestBody);
    }
}
