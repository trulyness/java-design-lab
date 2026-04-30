package com.design.lab.webhook.exceptions;

public class EventCannotBeDelivered extends RuntimeException {
    public EventCannotBeDelivered(final String eventId) {
        super("Event with Id " + eventId + " cannot be delivered as it's not in progress state!");
    }
}