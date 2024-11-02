package com.good.ivrstand.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Сервис для раобты с базой Qdrant
 */
@Component
@Slf4j
public class QdrantService {

    private final FlaskApiVectorSearchService flaskApiVectorSearchService;

    public QdrantService(FlaskApiVectorSearchService flaskApiVectorSearchService) {
        this.flaskApiVectorSearchService = flaskApiVectorSearchService;
    }

    /**
     * Синхронизирует базу данных Qdrant с данными в PostgreSQL
     */
    public void syncDatabase() {
        flaskApiVectorSearchService.syncDatabase();
        log.info("Базы данных синхронизированы");
    }
}
