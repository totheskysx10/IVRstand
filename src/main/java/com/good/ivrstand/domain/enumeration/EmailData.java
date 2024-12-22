package com.good.ivrstand.domain.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Данные для отправки письма на электронную почту.
 */
@Getter
@AllArgsConstructor
public enum EmailData {

    /**
     * Сброс пароля
     */
    RESET_PASSWORD("IVRstand - Восстановление пароля",
            """
            <html>
                <body>
                    <p>Чтобы сменить пароль и восстановить доступ, пройдите по ссылке (действует в течение 20 минут):</p>
                    <a href="%s%s&token=%s">Сбросить пароль</a>
                </body>
            </html>"""),

    /**
     * Подтверждение Email
     */
    CONFIRM_EMAIL("IVRstand - Подтверждение почты",
            """
            <html>
                <body>
                    <p>Чтобы подтвердить адрес электронной почты, пройдите по ссылке:</p>
                    <a href="%s%s">Подтвердить</a>
                </body>
            </html>""");

    /**
     * Тема письма
     */
    private final String emailSubject;

    /**
     * Текст письма
     */
    private final String emailMessage;
}