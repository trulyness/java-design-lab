package com.design.lab.webhook.model;

import java.time.Instant;

import com.design.lab.webhook.enums.Status;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class WebhookEvent {
    private final String eventId;
    private final String tenantId;
    private final String endpointId;
    private final String payload;
    @Setter
    private Instant createdAt;
    @Setter
    private Instant nextAttemptAt;
    @Setter
    private int attemptCount;
    @Setter
    private int maxRetries;
    @Setter
    private Status status;
}
