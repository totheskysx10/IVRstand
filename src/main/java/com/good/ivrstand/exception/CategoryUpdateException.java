package com.good.ivrstand.exception;

/**
 * Ошибка обновления данных категории
 */
public class CategoryUpdateException extends Exception {
  public CategoryUpdateException(String message) {
    super(message);
  }
}
