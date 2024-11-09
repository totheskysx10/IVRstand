package com.good.ivrstand.extern.api.controller;

import com.good.ivrstand.extern.infrastructure.bot.BotService;
import com.good.ivrstand.exception.NoChatsException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@Tag(name = "NotificationController", description = "Контроллер для управления уведомлениями")
public class NotificationController {

    private final BotService botService;

    @Autowired
    public NotificationController(BotService botService) {
        this.botService = botService;
    }

    @Operation(summary = "Отправить сообщение о вызове помощи", description = "Отправление сообщения о вызове помощи в Telegram-бот сотрудникам, которые подписались на уведомления")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное выполнение запроса"),
            @ApiResponse(responseCode = "204", description = "Нет чатов для отправки уведомления")
    })
    @PostMapping("/help")
    public ResponseEntity<Void> sendHelpMessage() {
        try {
            botService.sendHelpMessage();
            return ResponseEntity.ok().build();
        } catch (NoChatsException e) {
            return ResponseEntity.noContent().build();
        }
    }
}
