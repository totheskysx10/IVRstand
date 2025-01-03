package com.good.ivrstand.app.service;

import com.good.ivrstand.app.service.externinterfaces.FlaskApiTtsService;
import com.good.ivrstand.app.service.externinterfaces.S3Service;
import com.good.ivrstand.exception.FileDuplicateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Сервис для работы с озвучкой
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
     * Форматирует описание для передачи в озвучку.
     *
     * @param text текст описания
     * @return массив с блоками форматированного текста
     */
    public String[] splitDescription(String text) {
        String result = text.replaceAll("\"description\":", "")
                .replaceAll("\\\\\\\\icon\\d+", "")
                .replaceAll("\\\\icon\\d+", "")
                .replace("{\"", "")
                .replace("\"}", "")
                .replace("{", "")
                .replace("}", "");
        String[] resultArray = result.split("\\\\n\\\\n|\\\\n");

        return Arrays.stream(resultArray)
                .filter(line -> !line.trim().isEmpty())
                .toArray(String[]::new);
    }

    /**
     * Отправляет запрос на генерацию аудио по тексту.
     * Загружает файл на S3.
     *
     * @param text текст для озвучки
     * @return ссылка на аудиофайл или пустая строка, если text - пустой
     */
    public String generateAudio(String text) throws IOException, FileDuplicateException {
        if (text.isBlank()) {
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
