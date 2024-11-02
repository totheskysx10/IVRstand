package com.good.ivrstand.extern.infrastructure.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Серфис шифрования паролей
 */
@Service
public class PasswordService {

    /**
     * Возвращает объект шифровальщика паролей.
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
