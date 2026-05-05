package com.design.lab.reservations.exception;

public class RoomNotAvailableException extends RuntimeException {
    public RoomNotAvailableException() {
        super("Room not available!");
    }
}
