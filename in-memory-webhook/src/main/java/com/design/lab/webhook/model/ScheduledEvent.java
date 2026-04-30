package com.design.lab.webhook.model;

import java.time.Instant;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ScheduledEvent implements Delayed {
    private final String eventId;
    private final Instant nextAttemptAt;
    @Override
    public int compareTo(final Delayed other) {
        final ScheduledEvent o = (ScheduledEvent) other;
        return this.nextAttemptAt.compareTo(o.nextAttemptAt);
    }
    @Override
    public long getDelay(final TimeUnit unit) {
        final long delayMillis = nextAttemptAt.toEpochMilli() - System.currentTimeMillis();
        return unit.convert(delayMillis, TimeUnit.MILLISECONDS);
    }
}
