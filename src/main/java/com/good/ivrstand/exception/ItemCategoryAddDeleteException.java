package com.good.ivrstand.exception;

/**
 * Ошибка, когда не удалось добавить/удалить услугу из категории или категорию из категории
 */
public class ItemCategoryAddDeleteException extends Exception {
    public ItemCategoryAddDeleteException(String message) {
        super(message);
    }
}
