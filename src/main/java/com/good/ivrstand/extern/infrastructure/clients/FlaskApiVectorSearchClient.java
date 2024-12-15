package com.good.ivrstand.extern.infrastructure.clients;

import com.good.ivrstand.extern.api.flaskRequests.AddTitleRequest;
import com.good.ivrstand.extern.api.flaskRequests.TitleRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * Feign-клиент векторного поиска
 */
@FeignClient(name = "flaskApiClient", url = "${flask-api.vector}")
public interface FlaskApiVectorSearchClient {

    /**
     * Запрос получения списка с Id найденных услуг.
     *
     * @param requestData запрос
     */
    @PostMapping("/get_emb")
    List<Long> getItemIds(@RequestBody Map<String, String> requestData);

    /**
     * Запрос добавления услуги в базу Qdrant.
     *
     * @param request запрос
     */
    @PostMapping(value = "/add_title", consumes = MediaType.APPLICATION_JSON_VALUE)
    void addTitle(@RequestBody AddTitleRequest request);

    /**
     * Запрос удаления услуги из базы Qdrant.
     *
     * @param request запрос
     */
    @PostMapping(value = "/delete_title", consumes = MediaType.APPLICATION_JSON_VALUE)
    void deleteTitle(@RequestBody TitleRequest request);

    /**
     * Запрос синхронизации базы Qdrant с базой PostreSQL.
     */
    @PostMapping(value = "/sync_database", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> syncDatabase();
}
