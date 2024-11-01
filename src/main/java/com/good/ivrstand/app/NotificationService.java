package com.good.ivrstand.app;

import com.good.ivrstand.domain.NotificationCategory;
import com.good.ivrstand.domain.NotificationChat;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Сервисный класс для выполнения команд бота
 */
@Component
public class NotificationService {

    @Value("${telegram.bot.help_password}")
    private String helpPassword;

    @Value("${telegram.bot.search_password}")
    private String searchPassword;

    private Set<String> commands;

    private final Map<NotificationCategory, String> associations = Map.of(
            NotificationCategory.HELP, "Вызов помощи",
            NotificationCategory.SEARCH_ERROR, "Ошибки поиска"
    );

    private final NotificationChatRepository notificationChatRepository;

    public NotificationService(NotificationChatRepository notificationChatRepository) {
        this.notificationChatRepository = notificationChatRepository;
    }

    @PostConstruct
    private void initCommands() {
        this.commands = Set.of(
                helpPassword, searchPassword, "/start", "/add", "/removehelp", "/removesearch"
        );
    }

    /**
     * Выполняет команду в боте
     * @param messageText текст сообщения
     * @param chatId Id чата
     * @param chats список чатов по chatId
     * @return сообщение с ответом
     */
    public String performCommand(String messageText, String chatId, List<NotificationChat> chats) {
        String response = "Ошибка распознавания команды";

        if (!validateCommand(messageText))
            return response;

        if (messageText.equals("/start"))
            response = performStartCommand(chats);
        else if (messageText.equals("/add"))
            response = "Введите пароль нужной вам категории";
        else if (messageText.equals(helpPassword))
            response = subscribeToCategory(NotificationCategory.HELP, chats, chatId);
        else if (messageText.equals(searchPassword))
            response = subscribeToCategory(NotificationCategory.SEARCH_ERROR, chats, chatId);
        else if (messageText.equals("/removehelp"))
            response = removeCategory(NotificationCategory.HELP, chats);
        else if (messageText.equals("/removesearch"))
            response = removeCategory(NotificationCategory.SEARCH_ERROR, chats);

        return response;
    }

    /**
     * Валидирует команды, отправленные боту
     * @param command команда
     * @return true, если команда валидна
     */
    private boolean validateCommand(String command) {
        return commands.contains(command);
    }

    /**
     * Выполняет команду "/start"
     * @param chats список чатов по chatId
     */
    private String performStartCommand(List<NotificationChat> chats) {
        if (chats.isEmpty()) {
            return "Чтобы подписаться на уведомления, введите пароль";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Ваши категории уведомлений: ");
            for (NotificationChat chat: chats) {
                sb.append("\n- ").append(associations.get(chat.getNotificationCategory()));
            }
            return sb.toString();
        }
    }

    /**
     * Подписаться на категорию уведомлений
     * @param category категория
     * @param chats список чатов по chatId
     * @param chatId Id чата, который нужно подписать
     */
    private String subscribeToCategory(NotificationCategory category, List<NotificationChat> chats, String chatId) {
        for (NotificationChat chat : chats) {
            if (chat.getNotificationCategory() == category) {
                return String.format("Вы уже подписаны на категорию уведомлений: %s", associations.get(category));
            }
        }

        NotificationChat notificationChat = NotificationChat.builder()
                .chatId(chatId)
                .notificationCategory(category)
                .build();
        notificationChatRepository.save(notificationChat);
        return String.format("Вы подписались на категорию уведомлений: %s", associations.get(category));
    }

    /**
     * Отписаться от категории уведомлений
     * @param category категория
     * @param chats список чатов по chatId
     */
    private String removeCategory(NotificationCategory category, List<NotificationChat> chats) {
        String response = "Ошибка отмены подписки на уведомления";
        if (chats.isEmpty())
            response = String.format("Вы не были подписаны на уведомления категории: %s", associations.get(category));

        for (NotificationChat chat : chats) {
            if (chat.getNotificationCategory() == category) {
                notificationChatRepository.deleteById(chat.getId());
                return String.format("Отменена подписка на категорию уведомлений: %s", associations.get(category));
            }
            else {
                response = String.format("Вы не были подписаны на уведомления категории: %s", associations.get(category));
            }
        }

        return response;
    }

    /**
     * Создаёт сообщение о вызове помощи на IVR-стендне
     */
    public String createHelpMessage() {
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = currentTime.format(formatter);
        return String.format("%s, %s: требуется помощь на IVR-стенде", LocalDate.now(), formattedTime);
    }
}
