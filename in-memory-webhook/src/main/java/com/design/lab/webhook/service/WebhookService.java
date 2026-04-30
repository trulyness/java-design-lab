package com.design.lab.webhook.service;

import java.time.Instant;
import java.util.UUID;

import com.design.lab.webhook.enums.Status;
import com.design.lab.webhook.model.WebhookEvent;
import com.design.lab.webhook.store.Store;

public class WebhookService {
    
    private final Store store;
    
    public WebhookService(final Store store) {
        this.store = store;
    }

    public String submitEvent(final String tenantId, 
                              final String endpointId, 
                              final String payload, 
                              final int maxRetries) {
        final WebhookEvent event = WebhookEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .createdAt(Instant.now())
                                .tenantId(tenantId)
                                .payload(payload)
                                .maxRetries(maxRetries)
                                .attemptCount(0)
                                .nextAttemptAt(Instant.now())
                                .status(Status.PENDING)
                                .endpointId(endpointId)
                                .build();
        store.submitEvent(event);    
        return event.getEventId();        
    }

    public void cancelEvent(final String eventId) {
        store.cancelEvent(eventId);
    }

    public WebhookEvent getEvent(final String eventId) {
        return store.getEvent(eventId);
    }

}
