package com.good.ivrstand.extern.infrastructure.authentication;

/**
 * Тип токена - используется для метода валидации,
 * так как у каждого типа свой ключ подписи
 */
public enum TokenType {

    /**
     * Токен доступа
     */
    ACCESS_TOKEN,

    /**
     * Токен обновления
     */
    REFRESH_TOKEN
}
