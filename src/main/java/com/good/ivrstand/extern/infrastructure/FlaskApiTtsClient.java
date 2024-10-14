package com.good.ivrstand.extern.infrastructure;

import com.good.ivrstand.extern.api.requests.SynthesizeRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "TtsClient", url = "${flask-api.tts}")
public interface FlaskApiTtsClient {

    @PostMapping(value = "/synthesize", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<byte[]> synthesizeSpeech(@RequestBody SynthesizeRequest request);
}
