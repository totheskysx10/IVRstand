package com.good.ivrstand.extern.api.flaskRequests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Запрос на обработку/удаление услуги из базы Qdrant
 */
@AllArgsConstructor
public class TitleRequest {

    @Getter
    @JsonProperty("text")
    private String text;
}
