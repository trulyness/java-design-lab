# In-Memory Webhook: Design Notes

## Key decisions
- `DelayQueue<ScheduledEvent>` is used as the scheduler.
  It gives “process only when due” behavior without building a separate timer loop.
- Event metadata is stored in a map (`eventId -> WebhookEvent`) for quick status lookup.
- Workers run in a fixed thread pool and repeatedly pull due events.
- Retries use a fixed delay (`RETRY = 30s`) and stop at `maxRetries`.

## Concurrency approach
- Store operations that mutate event state are protected by a write lock.
- When a due event is picked, status is atomically moved from `PENDING` to `IN_PROGRESS`.
  This is what prevents two workers from processing the same event at the same time.
- Cancellation is allowed only from `PENDING`.
