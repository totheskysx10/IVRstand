package com.good.ivrstand.extern.api.controller;

import com.good.ivrstand.extern.infrastructure.bot.HelpEvent;
import com.good.ivrstand.extern.infrastructure.bot.TelegramBot;
import com.good.ivrstand.exception.NoChatsException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@Tag(name = "NotificationController", description = "Контроллер для управления уведомлениями")
public class NotificationController {


    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public NotificationController(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Operation(summary = "Отправить сообщение о вызове помощи", description = "Отправление сообщения о вызове помощи в Telegram-бот сотрудникам, которые подписались на уведомления")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное выполнение запроса"),
    })
    @PostMapping("/help")
    public ResponseEntity<Void> sendHelpMessage() {
        eventPublisher.publishEvent(new HelpEvent(this));
        return ResponseEntity.ok().build();
    }
}
