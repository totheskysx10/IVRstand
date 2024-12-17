package com.good.ivrstand.exception.notfound;

/**
 * Ошибка, когда пользователь не найден
 */
public class UserNotFoundException extends Exception {
    public UserNotFoundException(String message) {
        super(message);
    }
}
