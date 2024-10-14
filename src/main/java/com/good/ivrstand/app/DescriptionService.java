package com.good.ivrstand.app;

import org.springframework.stereotype.Component;

/**
 * Сервисный класс для работы с описаниями
 */
@Component
public class DescriptionService {

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
                .replace("\"}", "");
        String[] resultArray = result.split("\\\\n\\\\n|\\\\n");

        return resultArray;
    }
}
