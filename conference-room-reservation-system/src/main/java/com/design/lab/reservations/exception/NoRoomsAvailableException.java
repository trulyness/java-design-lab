package com.design.lab.reservations.exception;

public class NoRoomsAvailableException extends RuntimeException {
    public NoRoomsAvailableException() {
        super("No rooms available!");
    }
}
