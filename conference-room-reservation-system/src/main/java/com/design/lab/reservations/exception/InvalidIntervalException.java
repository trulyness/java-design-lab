package com.design.lab.reservations.exception;

public class InvalidIntervalException extends RuntimeException {
    public InvalidIntervalException() {
        super("Invalid time interval! startTime must be before endTime.");
    }
}
