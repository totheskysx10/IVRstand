package com.good.ivrstand.app;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Сервисный класс для работы с озвучкой
 */
@Component
@Slf4j
public class SpeechService {
    private final S3Service s3Service;
    private final FlaskApiTtsService flaskApiTtsService;

    public SpeechService(S3Service s3Service, FlaskApiTtsService flaskApiTtsService) {
        this.s3Service = s3Service;
        this.flaskApiTtsService = flaskApiTtsService;
    }

    /**
     * Генерирует аудио, отправляя запрос на интерфейс взаимодействия с Flask.
     * Загружает файл на S3.
     *
     * @param text текст для озвучки
     * @return ссылка на аудиофайл
     */
    public String generateAudio(String text) throws IOException {
        if (text.isEmpty()) {
            return "";
        }

        try {
            byte[] audioBytes = flaskApiTtsService.generateSpeech(text);

            try (InputStream audioInputStream = new ByteArrayInputStream(audioBytes)) {
                 MultipartFile multipartFile = new MockMultipartFile(
                         "file",
                         "audio.wav",
                         "audio/wav",
                         IOUtils.toByteArray(audioInputStream)
                 );
                 log.info("Сгенерирован аудиофайл");
                 return s3Service.uploadFile(multipartFile, "audio");
            }
        } catch (IOException ex) {
            throw new IOException("Error generating or uploading file", ex);
        }
    }
}
