package com.good.ivrstand.extern.infrastructure.bot;

import com.good.ivrstand.app.repository.NotificationChatRepository;
import com.good.ivrstand.app.service.NotificationService;
import com.good.ivrstand.domain.enumeration.NotificationCategory;
import com.good.ivrstand.domain.NotificationChat;
import com.good.ivrstand.exception.NoChatsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис Telegram-бота
 */
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final NotificationChatRepository notificationChatRepository;
    private final NotificationService notificationService;
    private final String username;

    public TelegramBot(NotificationChatRepository notificationChatRepository,
                       NotificationService notificationService,
                       @Value("${telegram.bot.token}") String botToken,
                       @Value("${telegram.bot.username}") String username) {
        super(botToken);
        this.username = username;
        this.notificationChatRepository = notificationChatRepository;
        this.notificationService = notificationService;
    }

    /**
     * Обрабатывает полученное в Telegram-бот сообщение.
     *
     * @param update полученное через бот сообщение
     */
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

    /**
     * @return имя пользователя в Telegram
     */
    @Override
    public String getBotUsername() {
        return username;
    }

    /**
     * Отправляет сообщение в указанный чат.
     *
     * @param message сообщение
     * @param id      id чата
     */
    private void sendMessageToChat(String message, String id) {
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
     * Отправляет сообщения о вызове помощи на IVR-стенде при публикации события
     */
    @EventListener
    private void sendHelpMessage(HelpEvent event) throws NoChatsException {
        List<NotificationChat> chats = notificationChatRepository.findAll();

        List<String> chatIds = new ArrayList<>();
        for (NotificationChat chat : chats) {
            if (chat.getNotificationCategory() == NotificationCategory.HELP)
                chatIds.add(chat.getChatId());
        }

        if (chatIds.isEmpty()) {
            throw new NoChatsException("Нет чатов для отправки сообщения!");
        }

        String message = notificationService.createHelpMessage();
        for (String chatId : chatIds)
            sendMessageToChat(message, chatId);
    }
}
