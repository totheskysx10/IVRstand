package com.good.ivrstand.extern.infrastructure;


import com.good.ivrstand.domain.AddTitleRequest;
import com.good.ivrstand.domain.TitleRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "flaskApiClient", url = "${flask-api.url}")
public interface FlaskApiVectorSearchClient {
    @PostMapping("/get_emb")
    List<Long> getEmbeddings(@RequestBody Map<String, Object> requestData);

    @PostMapping(value = "/add_title", consumes = MediaType.APPLICATION_JSON_VALUE)
    void addTitle(@RequestBody AddTitleRequest request);

    @PostMapping(value = "/delete_title", consumes = MediaType.APPLICATION_JSON_VALUE)
    void deleteTitle(@RequestBody TitleRequest request);

    @PostMapping(value = "/sync_database", consumes = MediaType.APPLICATION_JSON_VALUE)
    void syncDatabase();
}
