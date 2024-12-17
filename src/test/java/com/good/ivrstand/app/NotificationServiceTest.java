package com.good.ivrstand.app;

import com.good.ivrstand.app.repository.NotificationChatRepository;
import com.good.ivrstand.app.service.NotificationService;
import com.good.ivrstand.domain.NotificationChat;
import com.good.ivrstand.domain.enumeration.NotificationCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

    @Mock
    private NotificationChatRepository notificationChatRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService("search", "help", notificationChatRepository);
        ReflectionTestUtils.invokeMethod(notificationService, "initCommands");
    }

    @Test
    void testPerformCommandValidStartCommandNoSubscriptions() {
        String messageText = "/start";
        String chatId = "12345";
        List<NotificationChat> chats = new ArrayList<>();
        String expectedResponse = "Чтобы подписаться на уведомления, введите пароль";

        String response = notificationService.performCommand(messageText, chatId, chats);

        assertEquals(expectedResponse, response);
    }

    @Test
    void testPerformCommandValidStartCommandWithSubscriptions() {
        String messageText = "/start";
        String chatId = "12345";
        List<NotificationChat> chats = List.of(
                new NotificationChat(1L, chatId, NotificationCategory.HELP),
                new NotificationChat(2L, chatId, NotificationCategory.SEARCH_ERROR)
        );
        String expectedResponse = "Ваши категории уведомлений: \n- Вызов помощи\n- Ошибки поиска";

        String response = notificationService.performCommand(messageText, chatId, chats);

        assertEquals(expectedResponse, response);
    }

    @Test
    void testPerformCommandSubscribeToCategoryNotSubscribed() {
        String messageText = "help";
        String chatId = "12345";
        List<NotificationChat> chats = new ArrayList<>();
        NotificationChat newChat = new NotificationChat(1L, chatId, NotificationCategory.HELP);
        String expectedResponse = "Вы подписались на категорию уведомлений: Вызов помощи";

        when(notificationChatRepository.save(any(NotificationChat.class))).thenReturn(newChat);

        String response = notificationService.performCommand(messageText, chatId, chats);

        assertEquals(expectedResponse, response);
        verify(notificationChatRepository).save(any(NotificationChat.class));
    }

    @Test
    void testPerformCommandSubscribeToCategoryAlreadySubscribed() {
        String messageText = "help";
        String chatId = "12345";
        List<NotificationChat> chats = List.of(
                new NotificationChat(1L, chatId, NotificationCategory.HELP)
        );
        String expectedResponse = "Вы уже подписаны на категорию уведомлений: Вызов помощи";

        String response = notificationService.performCommand(messageText, chatId, chats);

        assertEquals(expectedResponse, response);
        verifyNoInteractions(notificationChatRepository);
    }

    @Test
    void testPerformCommandRemoveCategory() {
        String messageText = "/removehelp";
        String chatId = "12345";
        List<NotificationChat> chats = List.of(
                new NotificationChat(1L, chatId, NotificationCategory.HELP)
        );
        String expectedResponse = "Отменена подписка на категорию уведомлений: Вызов помощи";

        doNothing().when(notificationChatRepository).deleteById(1L);

        String response = notificationService.performCommand(messageText, chatId, chats);

        assertEquals(expectedResponse, response);
        verify(notificationChatRepository).deleteById(1L);
    }

    @Test
    void testPerformCommandRemoveCategoryNotSubscribed() {
        String messageText = "/removehelp";
        String chatId = "12345";
        List<NotificationChat> chats = new ArrayList<>();
        String expectedResponse = "Вы не были подписаны на уведомления категории: Вызов помощи";

        String response = notificationService.performCommand(messageText, chatId, chats);

        assertEquals(expectedResponse, response);
        verifyNoInteractions(notificationChatRepository);
    }

    @Test
    void testCreateHelpMessage() {
        String result = notificationService.createHelpMessage();

        assertNotNull(result);
        assertTrue(result.contains("требуется помощь на IVR-стенде"));
    }
}
