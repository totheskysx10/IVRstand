package com.good.ivrstand.exception.notfound;

/**
 * Ошибка, когда дополнение не найдено
 */
public class AdditionNotFoundException extends Exception {
    public AdditionNotFoundException(String message) {
        super(message);
    }
}
