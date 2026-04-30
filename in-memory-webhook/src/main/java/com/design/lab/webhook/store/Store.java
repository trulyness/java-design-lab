package com.design.lab.webhook.store;

import java.util.Optional;

import com.design.lab.webhook.model.ScheduledEvent;
import com.design.lab.webhook.model.WebhookEvent;

public interface Store {

    void submitEvent(final WebhookEvent event);
    void cancelEvent(final String eventId);
    WebhookEvent getEvent(final String eventId);
    Optional<ScheduledEvent> getNextReadyEvent() throws InterruptedException;
    void markEventDelivered(final String eventId);
    void markEventForRetry(final String eventId);
}
