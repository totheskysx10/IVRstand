package com.good.ivrstand.extern.infrastructure;

import com.good.ivrstand.app.MailBuilder;
import com.good.ivrstand.domain.EmailData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultMailBuilder implements MailBuilder {

    @Value("${auth.reset-password.link}")
    private String resetPasswordLink;

    @Value("${auth.confirm-email.link}")
    private String confirmEmailLink;

    public EmailData buildResetPasswordMessage(String receiver, String id, String token) {
        String emailSubject = "IVRstand - Восстановление пароля";
        String emailMessage = "Чтобы сменить пароль и восстановить доступ, пройдите по ссылке (действует в течение 20 минут): \n" + resetPasswordLink + id + "&token=" + token;

        return new EmailData(receiver, emailSubject, emailMessage);
    }

    public EmailData buildConfirmEmailMessage(String receiver, String id) {
        String emailSubject = "IVRstand - Подтверждение почты";
        String emailMessage = "Чтобы подтвердить адрес электронной почты, пройдите по ссылке: \n" + confirmEmailLink + id;

        return new EmailData(receiver, emailSubject, emailMessage);
    }
}
