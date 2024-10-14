package com.good.ivrstand.app;

/**
 * Интерфейс для сервиса Flask API озвучки текста.
 */
public interface FlaskApiTtsService {

    /**
     * Генерирует голос из текста.
     *
     * @param text текст
     * @return массив байт файла озвучки
     */
    byte[] generateSpeech(String text);
}
