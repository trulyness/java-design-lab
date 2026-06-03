package com.design.lab.splitwise.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(final String email) {
        super("User with email " + email + " already exists!");
    }
}
