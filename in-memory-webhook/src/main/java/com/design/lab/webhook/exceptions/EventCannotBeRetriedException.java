package com.design.lab.webhook.exceptions;

public class EventCannotBeRetriedException extends RuntimeException {
    public EventCannotBeRetriedException(final String eventId) {
        super("Event with Id " + eventId + " cannot be retried!");
    }
}