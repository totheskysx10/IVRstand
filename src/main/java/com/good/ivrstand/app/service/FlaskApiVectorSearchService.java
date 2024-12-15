package com.good.ivrstand.app.service;

import com.good.ivrstand.exception.ItemsFindException;
import com.good.ivrstand.extern.api.flaskRequests.AddTitleRequest;
import com.good.ivrstand.extern.api.flaskRequests.TitleRequest;

import java.util.List;

/**
 * Сервис векторного поиска.
 */
public interface FlaskApiVectorSearchService {

    /**
     * Получает найденные Id услуг по запросу.
     *
     * @param request запрос
     * @return список идентификаторов услуг
     */
    List<Long> getItemIds(String request);

    /**
     * Добавляет услугу в базу Qdrant
     *
     * @param request запрос
     */
    void addTitle(AddTitleRequest request);

    /**
     * Удаляет услугу из базы Qdrant
     *
     * @param request запрос
     */
    void deleteTitle(TitleRequest request);

    /**
     * Синхронизирует базу данных Qdrant с данными в PostgreSQL
     */
    void syncDatabase() throws ItemsFindException;
}

