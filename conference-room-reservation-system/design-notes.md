# Conference Room Reservation System: Design Notes

- `Location` is currently modeled as an enum to move fast; this can later be replaced by a richer location model if needed.
- Reservation lifecycle keeps history: cancellation marks `Reservation.status = CANCELLED` and removes it from the room’s active booking calendar. Improvement idea: maintain a separate `reservationHistory` map/index and move cancelled or old reservations there to keep active-path data lean while preserving audit/history access.
- Room availability uses half-open interval semantics `[startTime, endTime)`, so back-to-back bookings are allowed.
- Room booking API uses `Optional<Reservation>` (`bookRoomIfAvailable`) instead of throwing for expected contention/miss cases.
- `InMemoryStore` books optimistically across candidate rooms and throws `NoRoomsAvailableException` only if all candidates fail.
- Employee reservation listing excludes cancelled reservations and remains sorted by start time.
- Input validation in `reserveRoom`: unknown employee throws `EmployeeNotFoundException`; invalid interval (`startTime >= endTime`) throws `InvalidIntervalException`.
- Room filtering applies capacity and optional required features (`containsAll` when `requiredFeatures` is present).
- Building/floor room topology is immutable after startup (`List.copyOf`/`Map.copyOf`) since rooms are preconfigured.
- Mutable runtime indexes use concurrent collections (`ConcurrentHashMap`, `ConcurrentSkipListMap`, `CopyOnWriteArrayList`) to reduce concurrency risks.

## Potential Enhancements (Not Implemented Yet)

- Current implementation intentionally stays in-memory for speed of iteration for LLD rounds; a SQL-backed design would likely simplify richer filtering, ordering, and history queries.
- Add API to list all currently available rooms for a given time window and filters (building, floor, capacity, features).
- Add richer employee metadata (for example: assigned building, assigned floor) and use it as fallback preference:
  - if building filter is absent, prefer employee’s assigned building
  - if preferred floor is absent, prefer employee’s assigned floor
- Improve room selection strategy when multiple rooms match:
  - prioritize minimum capacity that satisfies requirements
  - then apply secondary tie-breakers for feature fit/proximity
- Support recurring weekly reservations with all-or-nothing semantics:
  - all occurrences must be bookable
  - if any occurrence fails, none are booked
  - return `List<Reservation>` for all occurrences on success
  - possible API shape:

```java
List<Reservation> reserveRecurringWeekly(
    String employeeId,
    Instant firstStart,
    Instant firstEnd,
    int numberOfWeeks,
    int requiredCapacity,
    Set<RoomFeature> requiredFeatures,
    Optional<String> preferredBuildingId
)
```

- Track meeting lifecycle beyond just `RESERVED`/`CANCELLED` (for example `IN_PROGRESS`, `COMPLETED`) to support queries like “upcoming meetings” and “meetings in progress”.
