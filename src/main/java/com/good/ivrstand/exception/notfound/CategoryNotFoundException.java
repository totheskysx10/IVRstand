package com.good.ivrstand.exception.notfound;

/**
 * Ошибка, когда категория не найдена
 */
public class CategoryNotFoundException extends Exception {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
