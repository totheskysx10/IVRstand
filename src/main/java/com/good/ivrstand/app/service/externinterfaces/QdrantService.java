package com.good.ivrstand.app.service.externinterfaces;

import com.good.ivrstand.exception.ItemsFindException;

/**
 * Сервис для раобты с базой Qdrant
 */
public interface QdrantService {

    /**
     * Синхронизирует базу данных Qdrant с PostgreSQL
     */
    void syncDatabase() throws ItemsFindException;
}
