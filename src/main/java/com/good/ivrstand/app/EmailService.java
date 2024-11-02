package com.good.ivrstand.app;

import com.good.ivrstand.domain.EmailData;

/**
 * Сервис отправки электронной почты.
 */
public interface EmailService {

    /**
     * Отправляет электронное письмо.
     *
     * @param emailData данные для отправки сообщения
     */
    void sendEmail(EmailData emailData);
}
