package com.good.ivrstand.app;

import com.good.ivrstand.domain.EmailData;

/**
 * Интерфейс для создания электронных сообщений
 */
public interface MailBuilder {

    /**
     * Создаёт письмо о сбросе пароля
     * @param receiver получатель
     * @param id индентификатор получателя
     * @param token токен сброса пароля
     * @return объект с данными о письме
     */
    EmailData buildResetPasswordMessage(String receiver, String id, String token);

    /**
     * Создаёт письмо о подтверждении эл. почты
     * @param receiver получатель
     * @param id индентификатор получателя
     * @return объект с данными о письме
     */
    EmailData buildConfirmEmailMessage(String receiver, String id);
}
