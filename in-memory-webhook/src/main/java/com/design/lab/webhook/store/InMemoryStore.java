package com.design.lab.webhook.store;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.design.lab.webhook.enums.Status;
import com.design.lab.webhook.exceptions.EventCannotBeCancelledException;
import com.design.lab.webhook.exceptions.EventCannotBeDelivered;
import com.design.lab.webhook.exceptions.EventCannotBeRetriedException;
import com.design.lab.webhook.exceptions.EventNotFoundException;
import com.design.lab.webhook.model.ScheduledEvent;
import com.design.lab.webhook.model.WebhookEvent;

public class InMemoryStore implements Store {
    private final Map<String,WebhookEvent> events; 
    private final ReentrantReadWriteLock lock;
    private final DelayQueue<ScheduledEvent> queue;
    private final static int RETRY = 30; 

    public InMemoryStore() {
        events = new HashMap<>();
        lock = new ReentrantReadWriteLock();
        queue = new DelayQueue<>();
    }

    @Override
    public void submitEvent(final WebhookEvent event) {
        final ScheduledEvent scheduledEvent = ScheduledEvent.builder()
        .eventId(event.getEventId())
        .nextAttemptAt(event.getNextAttemptAt())
        .build();
        try {
            lock.writeLock().lock();
            events.put(event.getEventId(),event);
            queue.add(scheduledEvent);
        } finally {
            lock.writeLock().unlock();
        }
        
    }

    @Override
    public void cancelEvent(final String eventId) {
        try {
            lock.writeLock().lock();
            final WebhookEvent event = validateAndGetEvent(eventId);

            if (event.getStatus()!=Status.PENDING) {
                throw new EventCannotBeCancelledException(eventId);
            }

            event.setStatus(Status.CANCELLED);

        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public WebhookEvent getEvent(final String eventId) {
        try {
            lock.readLock().lock();
            return validateAndGetEvent(eventId);
        } finally {
            lock.readLock().unlock();
        }
    } 

    

    private boolean isEventReady(final String eventId) {
        try {
            lock.writeLock().lock();
            final WebhookEvent event = validateAndGetEvent(eventId);

            if (event.getStatus()!=Status.PENDING) {
                return false;
            }

            event.setStatus(Status.IN_PROGRESS);
            event.setAttemptCount(event.getAttemptCount()+1);
            return true;

        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<ScheduledEvent> getNextReadyEvent() throws InterruptedException {

        final ScheduledEvent event = queue.take();
        if (isEventReady(event.getEventId())) {
            return Optional.of(event);
        }

        return Optional.empty();
    }

    @Override
    public void markEventDelivered(final String eventId) {
        try {
            lock.writeLock().lock();
            final WebhookEvent event = validateAndGetEvent(eventId);
            
            if (event.getStatus()!=Status.IN_PROGRESS) {
                throw new EventCannotBeDelivered(eventId);
            }

            event.setStatus(Status.DELIVERED);

        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void markEventForRetry(final String eventId) {
        try {
            lock.writeLock().lock();
            final WebhookEvent event = validateAndGetEvent(eventId);
            if (event.getStatus()!=Status.IN_PROGRESS) {
                throw new EventCannotBeRetriedException(eventId);
            }

            if (event.getAttemptCount()==event.getMaxRetries()) {
                event.setStatus(Status.FAILED);
                return;
            }
            final Instant nextAttemptAt = Instant.now().plusSeconds(RETRY);
            event.setStatus(Status.PENDING);
            event.setNextAttemptAt(nextAttemptAt);
            final ScheduledEvent scheduledEvent = ScheduledEvent.builder().eventId(eventId).nextAttemptAt(nextAttemptAt).build();
            queue.add(scheduledEvent);

        } finally {
            lock.writeLock().unlock();
        }
    }

    private WebhookEvent validateAndGetEvent(final String eventId) {
        if (!events.containsKey(eventId)) {
            throw new EventNotFoundException(eventId);
        }
        return events.get(eventId);
    }

}