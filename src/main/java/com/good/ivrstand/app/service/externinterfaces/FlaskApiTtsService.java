package com.good.ivrstand.app.service.externinterfaces;

/**
 * Сервис озвучки текста.
 */
public interface FlaskApiTtsService {

    /**
     * Генерирует голос по тексту.
     *
     * @param text текст
     * @return массив байт файла озвучки
     */
    byte[] generateSpeech(String text);
}
