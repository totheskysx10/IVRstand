package com.good.ivrstand.extern.api.controller;

import com.good.ivrstand.extern.infrastructure.service.DefaultQdrantService;
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

    private final DefaultQdrantService defaultQdrantService;

    public QdrantController(DefaultQdrantService defaultQdrantService) {
        this.defaultQdrantService = defaultQdrantService;
    }

    @Operation(summary = "Синхронизировать базы данных", description = "Синхронизирует базу данных Qdrant с данными в PostgreSQL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Базы синхронизированы успешно"),
            @ApiResponse(responseCode = "500", description = "Ошибка синхронизации")
    })
    @PostMapping("/sync")
    public ResponseEntity<Void> syncDatabase() {
        try {
            defaultQdrantService.syncDatabase();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
