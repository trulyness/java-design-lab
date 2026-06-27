package com.design.lab.timeseriesdb.exceptions;

public class InvalidRangeException extends RuntimeException {
    public InvalidRangeException() {
        super("Start time cannot be greater than end time.");
    }
}
