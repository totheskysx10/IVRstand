package com.good.ivrstand.extern.infrastructure;

import com.good.ivrstand.app.FlaskApiVectorSearchService;
import com.good.ivrstand.extern.api.requests.AddTitleRequest;
import com.good.ivrstand.extern.api.requests.TitleRequest;
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

    public List<Long> getEmbeddings(List<String> dialog) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("dialog", dialog);

        return flaskApiVectorSearchClient.getEmbeddings(requestData);
    }

    public void addTitle(AddTitleRequest addTitleRequest) {
        flaskApiVectorSearchClient.addTitle(addTitleRequest);
    }

    public void deleteTitle(TitleRequest title) {
        flaskApiVectorSearchClient.deleteTitle(title);
    }

    public void syncDatabase() {
        flaskApiVectorSearchClient.syncDatabase();
    }
}
