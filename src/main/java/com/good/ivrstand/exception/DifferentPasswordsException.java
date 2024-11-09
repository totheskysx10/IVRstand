package com.good.ivrstand.exception;

public class DifferentPasswordsException extends RuntimeException {
    public DifferentPasswordsException(String message) {
        super(message);
    }
}
