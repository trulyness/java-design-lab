package com.design.lab.webhook.service;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.design.lab.webhook.client.WebhookClient;
import com.design.lab.webhook.model.ScheduledEvent;
import com.design.lab.webhook.model.WebhookEvent;
import com.design.lab.webhook.store.Store;

public class WebhookWorker {
    private final AtomicBoolean started;
    private final ExecutorService eventScheduler;
    private final WebhookClient client;
    private final Store store;
    private final int eventSchedulerThreads;

    public WebhookWorker(final int eventSchedulerThreads, final WebhookClient client, final Store store) {
        this.started = new AtomicBoolean(false);
        this.eventScheduler = Executors.newFixedThreadPool(eventSchedulerThreads);
        this.client = client;
        this.store = store;
        this.eventSchedulerThreads = eventSchedulerThreads;
    }

    private void processEvents() {
        while(started.get()) {
            try {
                final Optional<ScheduledEvent> scheduledEvent = store.getNextReadyEvent();
                if (scheduledEvent.isEmpty()) continue;
                final String eventId = scheduledEvent.get().getEventId();
                final WebhookEvent event = store.getEvent(eventId);
                if (client.deliver(event)) {
                    store.markEventDelivered(eventId);
                } else {
                    store.markEventForRetry(eventId);
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }   
            
        }
    }

    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        
        for (int i=0;i<eventSchedulerThreads;i++) {
            eventScheduler.submit(this::processEvents);
        }
        
    }

    public void stopService() {
        started.set(false);
        eventScheduler.shutdownNow();
    }
}
