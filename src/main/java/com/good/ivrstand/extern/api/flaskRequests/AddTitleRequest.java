package com.good.ivrstand.extern.api.flaskRequests;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Запрос на добавление услуги в базу Qdrant
 */
public class AddTitleRequest extends TitleRequest {
    @JsonProperty("id")
    private long id;

    public AddTitleRequest(String text, long id) {
        super(text);
        this.id = id;
    }
}
