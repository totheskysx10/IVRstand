package com.good.ivrstand.extern.infrastructure.service;

import com.good.ivrstand.app.service.externinterfaces.FlaskApiTtsService;
import com.good.ivrstand.extern.api.flaskRequests.SynthesizeRequest;
import com.good.ivrstand.extern.infrastructure.clients.FlaskApiTtsClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Сервис озвучки текста
 */
@Component
public class DefaultFlaskApiTtsService implements FlaskApiTtsService {

    private final FlaskApiTtsClient flaskApiTtsClient;

    public DefaultFlaskApiTtsService(FlaskApiTtsClient flaskApiTtsClient) {
        this.flaskApiTtsClient = flaskApiTtsClient;
    }

    /**
     * Вызывает в Feign-клиенте метод генерации аудио по тексту.
     */
    public byte[] generateSpeech(String text) {
        SynthesizeRequest request = new SynthesizeRequest(text);
        ResponseEntity<byte[]> response = flaskApiTtsClient.synthesizeSpeech(request);
        return response.getBody();
    }
}
