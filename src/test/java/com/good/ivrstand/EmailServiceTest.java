package com.good.ivrstand;

import com.good.ivrstand.domain.EmailData;
import com.good.ivrstand.extern.infrastructure.DefaultEmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private DefaultEmailService emailService;

    @Test
    public void testSendEmail_Success() {
        String address = "recipient@example.com";
        String subject = "Test Subject";
        String message = "Test Message";

        emailService.sendEmail(new EmailData(address, subject, message));

        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }
}