package com.design.lab.webhook.exceptions;

public class EventCannotBeCancelledException extends RuntimeException {
    public EventCannotBeCancelledException(final String eventId) {
        super("Event with Id " + eventId + " cannot be cancelled!");
    }
}
