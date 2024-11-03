package com.good.ivrstand.extern.infrastructure;

import com.good.ivrstand.extern.api.flaskRequests.SynthesizeRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign-клиент приложения для генерации озвучки
 */
@FeignClient(name = "TtsClient", url = "${flask-api.tts}")
public interface FlaskApiTtsClient {

    /**
     * Запрос на генерацию аудио по тексту.
     *
     * @param request запрос
     * @return сгенерированное аудио в виде массива байт
     */
    @PostMapping(value = "/synthesize", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<byte[]> synthesizeSpeech(@RequestBody SynthesizeRequest request);
}
