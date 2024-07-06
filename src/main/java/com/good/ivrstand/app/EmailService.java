package com.good.ivrstand.app;

import com.good.ivrstand.domain.EmailData;

/**
 * Интерфейс для отправки электронной почты.
 */
public interface EmailService {

    /**
     * Отправляет электронное письмо на указанный адрес с заданной темой и сообщением.
     *
     * @param emailData данные для отправки сообщения
     */
    void sendEmail(EmailData emailData);
}
