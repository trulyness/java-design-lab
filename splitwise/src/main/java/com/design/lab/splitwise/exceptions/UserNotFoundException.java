package com.design.lab.splitwise.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(final String email) {
        super("User with email " + email + " was not found!");
    }
}
