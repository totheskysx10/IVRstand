package com.good.ivrstand.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddTitleRequest extends TitleRequest {
    @JsonProperty("id")
    private long id;

    public AddTitleRequest(String text, long id) {
        super(text);
        this.id = id;
    }
}
