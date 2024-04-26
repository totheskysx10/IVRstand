package com.good.ivrstand.extern.api;

import com.good.ivrstand.domain.NotificationCategory;
import com.good.ivrstand.app.NotificationChatRepository;
import com.good.ivrstand.app.NotificationService;
import com.good.ivrstand.domain.NotificationChat;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "https://good-web-ivr.netlify.app"})
@Tag(name = "NotificationController", description = "Контроллер для управления уведомлениями")
public class NotificationController {

    private final NotificationService notificationService;

    private final NotificationChatRepository notificationChatRepository;

    @Autowired
    public NotificationController(NotificationService notificationService, NotificationChatRepository notificationChatRepository) {
        this.notificationService = notificationService;
        this.notificationChatRepository = notificationChatRepository;
    }

    @Operation(summary = "Отправить сообщение о вызове помощи", description = "Отправление сообщения о вызове помощи в Telegram-бот сотрудникам, которые подписались на уведомления")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное выполнение запроса"),
            @ApiResponse(responseCode = "204", description = "Нет чатов для отправки уведомления")
    })
    @PostMapping("/help")
    public ResponseEntity<Void> sendHelpMessage() {
        List<NotificationChat> chats = notificationChatRepository.findAll();

        List<String> chatIds = new ArrayList<>();
        for (NotificationChat chat: chats) {
            if (chat.getNotificationCategory() == NotificationCategory.HELP)
                chatIds.add(chat.getChatId());
        }

        if (chatIds.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = currentTime.format(formatter);
        String message = LocalDate.now() + ", " + formattedTime + ": требуется помощь на IVR-стенде";
        notificationService.sendMessageToChats(message, chatIds);
        return ResponseEntity.ok().build();
    }
}
