package com.good.ivrstand.extern.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

/**
 * Запрос на генерацию аудио по тексту
 */
@AllArgsConstructor
public class SynthesizeRequest {
    @JsonProperty("text")
    private String text;
}
