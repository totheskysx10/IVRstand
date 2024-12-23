package com.good.ivrstand.extern.api.controller;

import com.good.ivrstand.app.service.externinterfaces.FlaskApiVectorSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/qdrant")
@Tag(name = "QdrantController", description = "Контроллер для управления базой Qdrant")
public class QdrantController {

    private final FlaskApiVectorSearchService flaskApiVectorSearchService;

    public QdrantController(FlaskApiVectorSearchService flaskApiVectorSearchService) {
        this.flaskApiVectorSearchService = flaskApiVectorSearchService;
    }

    @Operation(summary = "Синхронизировать базы данных", description = "Синхронизирует базу данных Qdrant с данными в PostgreSQL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Базы синхронизированы успешно"),
            @ApiResponse(responseCode = "500", description = "Ошибка синхронизации")
    })
    @PostMapping("/sync")
    public ResponseEntity<Void> syncDatabase() {
        try {
            flaskApiVectorSearchService.syncDatabase();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
