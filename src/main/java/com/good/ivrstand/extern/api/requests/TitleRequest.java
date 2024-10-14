package com.good.ivrstand.extern.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TitleRequest {
    @JsonProperty("text")
    private String text;
}