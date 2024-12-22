package com.good.ivrstand.app;

import com.good.ivrstand.extern.infrastructure.service.DefaultEmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage mimeMessage;

    private DefaultEmailService emailService;

    @BeforeEach
    public void setUp() {
        this.emailService = new DefaultEmailService(javaMailSender, "test@mail.ru");
    }

    @Test
    public void testSendEmail() {
        String receiver = "test@domain.com";
        String subject = "Test Subject";
        String content = "Test Content";

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(receiver, subject, content);

        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
}