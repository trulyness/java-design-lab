package com.trading.exceptions;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(final String message) {
        super(message);
    }
}
