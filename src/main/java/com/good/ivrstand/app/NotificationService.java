package com.good.ivrstand.app;

import com.good.ivrstand.domain.BotProperties;
import com.good.ivrstand.domain.NotificationCategory;
import com.good.ivrstand.domain.NotificationChat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationService extends TelegramLongPollingBot {

    @Value("${telegram.bot.help_password}")
    private String helpPassword;

    @Value("${telegram.bot.search_password}")
    private String searchPassword;

    private final BotProperties botProperties;

    private final NotificationChatRepository notificationChatRepository;

    public NotificationService(@Value("${telegram.bot.token}") String botToken, BotProperties botProperties, NotificationChatRepository notificationChatRepository) {
        super(botToken);
        this.botProperties = botProperties;
        this.notificationChatRepository = notificationChatRepository;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = String.valueOf(update.getMessage().getChatId());
            List<NotificationChat> chats = notificationChatRepository.findByChatId(chatId);

            List<String> categories = new ArrayList<>();
            for (NotificationChat chat: chats) {
                switch (chat.getNotificationCategory()) {
                    case HELP:
                        categories.add("Вызов помощи");
                        break;
                    case SEARCH_ERROR:
                        categories.add("Сообщения об ошибках поиска");
                        break;
                }
            }

            if (messageText.equals("/start")) {
                if (chats.isEmpty()) {
                    sendMessageToChat("Чтобы подписаться на уведомления, введите пароль", chatId);
                } else {
                    String categoriesMessage = categories.stream().collect(Collectors.joining(", "));
                    sendMessageToChat("Ваши категории уведомлений: " + categoriesMessage, chatId);
                }
            } else if (messageText.equals("/add")) {
                sendMessageToChat("Введите пароль нужной вам категории", chatId);
            } else if (messageText.equals(helpPassword)) {
                boolean helpChatFound = false;

                for (NotificationChat chat : chats) {
                    if (chat.getNotificationCategory() == NotificationCategory.HELP) {
                        sendMessageToChat("Вы уже подписаны на уведомления о вызове помощи", chatId);
                        helpChatFound = true;
                        break;
                    }
                }

                if (!helpChatFound) {
                    NotificationChat notificationChat = NotificationChat.builder()
                            .chatId(chatId)
                            .notificationCategory(NotificationCategory.HELP)
                            .build();
                    notificationChatRepository.save(notificationChat);
                    sendMessageToChat("Вы подписались на уведомления о вызове помощи", chatId);
                }
            } else if (messageText.equals(searchPassword)) {
                boolean searchChatFound = false;

                for (NotificationChat chat : chats) {
                    if (chat.getNotificationCategory() == NotificationCategory.SEARCH_ERROR) {
                        sendMessageToChat("Вы уже подписаны на уведомления об ошибках поиска", chatId);
                        searchChatFound = true;
                        break;
                    }
                }

                if (!searchChatFound) {
                    NotificationChat notificationChat = NotificationChat.builder()
                            .chatId(chatId)
                            .notificationCategory(NotificationCategory.SEARCH_ERROR)
                            .build();
                    notificationChatRepository.save(notificationChat);
                    sendMessageToChat("Вы подписались на уведомления об ошибках поиска", chatId);
                }
            } else if (messageText.equals("/removehelp")) {
                for (NotificationChat chat : chats) {
                    if (chat.getNotificationCategory() == NotificationCategory.HELP) {
                        notificationChatRepository.deleteById(chat.getId());
                        sendMessageToChat("Подписка на уведомления о вызове помощи отменена", chatId);
                        break;
                    }
                    else {
                        sendMessageToChat("Вы не были подписаны на уведомления о вызове помощи", chatId);
                    }
                }
            } else if (messageText.equals("/removesearch")) {
                for (NotificationChat chat : chats) {
                    if (chat.getNotificationCategory() == NotificationCategory.SEARCH_ERROR) {
                        notificationChatRepository.deleteById(chat.getId());
                        sendMessageToChat("Подписка на уведомления об ошибках поиска отменена", chatId);
                        break;
                    }
                    else {
                        sendMessageToChat("Вы не были подписаны на уведомления об ошибках поиска", chatId);
                    }
                }
            } else
                sendMessageToChat("Ошибка распознавания команды", chatId);
        }
    }

    @Override
    public String getBotUsername() {
        return botProperties.getUsername();
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

    public void sendMessageToChats(String message, List<String> ids) {
        SendMessage tgMessage = new SendMessage();
        tgMessage.setText(message);
        for (String id: ids) {
            tgMessage.setChatId(id);
            try {
                execute(tgMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
