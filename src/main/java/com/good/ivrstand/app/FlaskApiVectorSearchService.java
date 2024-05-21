package com.good.ivrstand.app;

import com.good.ivrstand.domain.TitleRequest;

import java.util.List;

/**
 * Интерфейс для сервиса Flask API векторного поиска.
 */
public interface FlaskApiVectorSearchService {

    /**
     * Получает найденные Id услуг для заданного списка запросов.
     *
     * @param dialog список строковых запросов
     * @return список идентификаторов услуг
     */
    List<Long> getEmbeddings(List<String> dialog);

    /**
     * Добавляет заголовок услуги в Flask-сервис.
     *
     * @param request объект запроса с данными
     */
    void addTitle(TitleRequest request);

    /**
     * Удаляет заголовок услуги из Flask-сервиса.
     *
     * @param title строка заголовка, который необходимо удалить
     */
    void deleteTitle(String title);
}

