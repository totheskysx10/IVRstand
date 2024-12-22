package com.good.ivrstand.extern.infrastructure.service;

import com.good.ivrstand.app.service.externinterfaces.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Сервис отправки Email
 */
@Component
public class DefaultEmailService implements EmailService {

    private final JavaMailSender javaMailSender;

    private final String emailFrom;

    public DefaultEmailService(JavaMailSender javaMailSender,
                               @Value("${spring.mail.username}") String emailFrom) {
        this.javaMailSender = javaMailSender;
        this.emailFrom = emailFrom;
    }

    /**
     * Отправляет сообщение на эл. почту
     *
     * @param receiver получатель
     * @param subject тема сообщения
     * @param content текст сообщения
     */
    @Async
    public void sendEmail(String receiver, String subject, String content) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(receiver);
            helper.setSubject(subject);
            helper.setText(content, true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
