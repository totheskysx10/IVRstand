package com.good.ivrstand.extern.infrastructure;

import com.good.ivrstand.app.FlaskApiTtsService;
import com.good.ivrstand.extern.api.requests.SynthesizeRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DefaultFlaskApiTtsService implements FlaskApiTtsService {

    private final FlaskApiTtsClient flaskApiTtsClient;

    public DefaultFlaskApiTtsService(FlaskApiTtsClient flaskApiTtsClient) {
        this.flaskApiTtsClient = flaskApiTtsClient;
    }

    public byte[] generateSpeech(String text) {
        SynthesizeRequest request = new SynthesizeRequest(text);
        ResponseEntity<byte[]> response = flaskApiTtsClient.synthesizeSpeech(request);
        return response.getBody();
    }
}
