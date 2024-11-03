package com.good.ivrstand.app;

import com.good.ivrstand.domain.NotificationChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationChatRepository extends JpaRepository<NotificationChat, Long> {
    void deleteById(long id);
    List<NotificationChat> findByChatId(String id);
}
