package com.good.ivrstand.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Данные для отправки письма на электронную почту.
 */
@AllArgsConstructor
public class EmailData {

    /**
     * Получатель письма
     */
    @Getter
    private String emailReceiver;

    /**
     * Тема письма
     */
    @Getter
    private String emailSubject;

    /**
     * Текст письма
     */
    @Getter
    private String emailMessage;
}