package com.good.ivrstand.exception;

/**
 * Ошибка изменения прав пользователя
 */
public class UserRolesException extends Exception {
    public UserRolesException(String message) {
        super(message);
    }
}
