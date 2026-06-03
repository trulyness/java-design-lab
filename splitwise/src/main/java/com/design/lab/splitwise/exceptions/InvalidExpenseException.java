package com.design.lab.splitwise.exceptions;

public class InvalidExpenseException extends RuntimeException {
    public InvalidExpenseException(final String message) {
        super(message);
    }
}
