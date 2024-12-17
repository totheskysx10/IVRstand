package com.good.ivrstand.exception.notfound;

/**
 * Ошибка, когда услуга не найдена
 */
public class ItemNotFoundException extends Exception {
    public ItemNotFoundException(String message) {
        super(message);
    }
}
