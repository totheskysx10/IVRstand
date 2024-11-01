package com.good.ivrstand.extern.infrastructure.bot;

import com.good.ivrstand.app.NotificationChatRepository;
import com.good.ivrstand.app.NotificationService;
import com.good.ivrstand.domain.NotificationCategory;
import com.good.ivrstand.domain.NotificationChat;
import com.good.ivrstand.exception.NoChatsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class BotService extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String username;

    private final NotificationChatRepository notificationChatRepository;

    private final NotificationService notificationService;

    public BotService(@Value("${telegram.bot.token}") String botToken, NotificationChatRepository notificationChatRepository, NotificationService notificationService) {
        super(botToken);
        this.notificationChatRepository = notificationChatRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = String.valueOf(update.getMessage().getChatId());
            List<NotificationChat> chats = notificationChatRepository.findByChatId(chatId);

            String response = notificationService.performCommand(messageText, chatId, chats);
            sendMessageToChat(response, chatId);
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    public void sendMessageToChat(String message, String id) {
        SendMessage tgMessage = new SendMessage();
        tgMessage.setText(message);
        tgMessage.setChatId(id);
        try {
            execute(tgMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправляет сообщения о вызове помощи на IVR-стенде
     */
    public void sendHelpMessage() {
        List<NotificationChat> chats = notificationChatRepository.findAll();

        List<String> chatIds = new ArrayList<>();
        for (NotificationChat chat: chats) {
            if (chat.getNotificationCategory() == NotificationCategory.HELP)
                chatIds.add(chat.getChatId());
        }

        if (chatIds.isEmpty()) {
            throw new NoChatsException("Нет чатов для отправки сообщения!");
        }

        String message = notificationService.createHelpMessage();
        for (String chatId: chatIds)
            sendMessageToChat(message, chatId);
    }
}
