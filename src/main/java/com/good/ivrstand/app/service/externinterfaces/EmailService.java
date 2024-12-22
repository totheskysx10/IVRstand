package com.good.ivrstand.app.service.externinterfaces;

/**
 * Сервис отправки электронной почты.
 */
public interface EmailService {

    /**
     * Отправляет электронное письмо.
     *
     * @param receiver получатель
     * @param subject тема сообщения
     * @param content текст сообщения
     */
    void sendEmail(String receiver, String subject, String content);
}
