package com.good.ivrstand.exception;

/**
 * Ошибка при обновлении данных услуги
 */
public class ItemUpdateException extends Exception {
    public ItemUpdateException(String message) {
        super(message);
    }
}
