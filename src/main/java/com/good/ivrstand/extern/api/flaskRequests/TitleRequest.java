package com.good.ivrstand.extern.api.flaskRequests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

/**
 * Запрос на обработку/удаление услуги из базы Qdrant
 */
@AllArgsConstructor
public class TitleRequest {
    @JsonProperty("text")
    private String text;
}
