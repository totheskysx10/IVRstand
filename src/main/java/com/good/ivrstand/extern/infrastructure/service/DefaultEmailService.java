package com.good.ivrstand.extern.infrastructure.service;

import com.good.ivrstand.app.service.externinterfaces.EmailService;
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

    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String emailFrom;

    public DefaultEmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * Отправляет сообщение на эл. почту
     *
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
