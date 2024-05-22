package com.good.ivrstand.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TitleRequest {
    @JsonProperty("text")
    private String text;
}
