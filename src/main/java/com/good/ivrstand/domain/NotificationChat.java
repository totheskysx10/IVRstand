package com.good.ivrstand.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Сущность чата для уведомлений.
 */
@Entity
@Table(name = "notification_chats")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class NotificationChat {

    /**
     * Идентификатор сущности.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private long Id;

    /**
     * Id чата для уведомлений.
     */
    @Column(name = "notification_chat_id")
    @Getter
    @NonNull
    private String chatId;

    /**
     * Категория уведомлений.
     */
    @Column(name = "notification_category")
    @Enumerated(EnumType.STRING)
    @Getter
    @NonNull
    private NotificationCategory notificationCategory;
}
