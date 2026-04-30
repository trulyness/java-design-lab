package com.design.lab.webhook.exceptions;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(final String eventId) {
        super("Event with Id " + eventId + " was not found!");
    }
}