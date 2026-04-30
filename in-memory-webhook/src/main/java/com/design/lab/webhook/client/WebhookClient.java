package com.design.lab.webhook.client;

import com.design.lab.webhook.model.WebhookEvent;

public interface WebhookClient {
    boolean deliver(final WebhookEvent event);
}