package com.good.ivrstand.extern.infrastructure;

import com.good.ivrstand.app.EmailService;
import com.good.ivrstand.domain.EmailData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Сервис отправки Email
 */
@Component
public class DefaultEmailService implements EmailService {

    @Value("${spring.mail.username}")
    private String emailFrom;
    private final JavaMailSender javaMailSender;

    public DefaultEmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * Отправляет сообщение на эл. почту
     * @param emailData данные для отправки сообщения
     */
    @Async
    public void sendEmail(EmailData emailData) {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom(emailFrom);
            simpleMailMessage.setTo(emailData.getEmailReceiver());
            simpleMailMessage.setSubject(emailData.getEmailSubject());
            simpleMailMessage.setText(emailData.getEmailMessage());
            javaMailSender.send(simpleMailMessage);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }
}
