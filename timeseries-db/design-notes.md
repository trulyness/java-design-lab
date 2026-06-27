# In-Memory Time Series Metrics Store: Design Notes

## Design Decisions

1. Use `long` for timestamps.

   Timestamps are represented as epoch milliseconds using `long`, not `Instant`. This matches the problem statement, keeps range comparisons simple, makes bucket math straightforward, and avoids timezone/date complexity.

2. Treat `COUNTER` and `GAUGE` as metric metadata.

   Both metric types are stored, but writes are handled the same way. Counter-specific behavior like rate/delta calculation is not required by the problem, but the type leaves room for future validation or counter-specific queries.

3. Keep `Metric` as definition-only.

   `Metric` stores `name`, `metricType`, and `description`. It does not store datapoints or locks. This keeps domain metadata separate from storage and concurrency concerns.

4. Store datapoints by metric and timestamp.

   Each metric has a `NavigableMap<Long, List<Datapoint>>`, where the key is `timestampMillis` and the value is all datapoints at that exact timestamp. This supports sorted range scans and multiple datapoints with the same timestamp.

5. Use `NavigableMap.subMap` for range queries.

   Raw queries use inclusive bounds with `subMap(startTimeMillis, true, endTimeMillis, true)`, matching the requirement that timestamps satisfy `timestamp >= startTime` and `timestamp <= endTime`.

6. Introduce `MetricState` as store-internal state.

   `MetricState` contains the `Metric`, the metric's `NavigableMap<Long, List<Datapoint>>`, and its `ReentrantReadWriteLock`. This keeps all mutable store state for one metric together.

7. Put `MetricState` in the `store` package.

   `MetricState` contains storage structures and a lock, so it is an implementation detail of `InMemoryStore`, not a reusable domain model.

8. Use one concurrent map for registered metrics.

   The store uses a `ConcurrentHashMap<String, MetricState>`. A single lookup gives access to the metric metadata, datapoints, and lock.

9. Use `putIfAbsent` for registration.

   Registration inserts the `MetricState` atomically. This prevents duplicate metric registration under concurrent calls and avoids check-then-put races.

10. Use per-metric `ReentrantReadWriteLock`.

    Each `MetricState` has its own lock. Writes to one metric do not block reads or writes for another metric. Multiple reads for the same metric can run concurrently, while writes are exclusive.

11. Use the write lock for datapoint writes.

    `writeMetricData` takes the metric's write lock because it mutates the metric's `TreeMap` and per-timestamp `ArrayList`.

12. Use the read lock for raw and latest queries.

    `getMetricData` and `getLatestMetricData` take the metric's read lock because they read from the metric's `TreeMap` and lists.

13. Aggregate from raw datapoints.

    `aggregateMetricData` first fetches raw datapoints for the requested range, then aggregates them. This keeps the implementation simple and reuses range-query logic.

14. Extract aggregation logic into a helper.

    Aggregation is centralized in `aggregateDatapoints`, which is reused by normal aggregation and bucketed aggregation.

15. Use `double` for aggregate results.

    All aggregate types return `double`, including `COUNT`, so the aggregate API has one consistent return type.

16. Throw when aggregating an empty range.

    Empty aggregate input throws `NoDatapointsFoundException`. This avoids misleading values such as `AVG = 0` when no datapoints exist.

17. Return `Optional<Datapoint>` for latest query.

    `getLatestMetricData` returns `Optional.empty()` if the metric exists but has no datapoints.

18. Bucket query returns `Map<Long, Double>`.

    Bucketed aggregation returns `bucketStartMillis -> aggregateValue`. A `TreeMap` is used so bucket results are ordered by bucket start time.

19. Skip empty buckets.

    Empty buckets are omitted because `Map<Long, Double>` cannot represent empty values cleanly without using `null`. A stricter interpretation of "one value per bucket" could use `Map<Long, OptionalDouble>` or a `BucketResult` model later.

20. Avoid double-counting bucket boundaries.

    Bucket end is computed with `Math.min(i + bucketSizeMillis - 1, endTimeMillis)` because range queries are inclusive. This prevents a datapoint exactly on a bucket boundary from appearing in two buckets.

21. Validate invalid ranges and bucket sizes.

    Invalid ranges throw `InvalidRangeException`. Non-positive bucket sizes throw `InvalidBucketSizeException`.

22. Keep `TimeSeriesService` thin.

    `TimeSeriesService` builds `Metric` objects and delegates storage/query behavior to `InMemoryStore`. The store owns persistence and concurrency.

23. Use "keep both" as the duplicate timestamp policy.

    If the same metric receives multiple writes with the same timestamp, all datapoints are retained in insertion order in the timestamp's `List<Datapoint>`. This avoids silently losing writes, works naturally with concurrent clients, and matches the current `NavigableMap<Long, List<Datapoint>>` storage model.

24. Support exact tag matching with a secondary index.

    `Metric` stores tags as `Map<String, String>`, and `TimeSeriesService` accepts tags during registration. Tag lookup uses an in-memory secondary index shaped as `tagKey -> tagValue -> metricNames`, allowing `findMetricsByTags` to return metrics that match all requested tag key-value pairs exactly.

25. Accept eventual consistency for the tag index.

    The tag index is updated after successful metric registration. There is a small window where a metric may be registered before all tag index entries are visible. This is acceptable for this design because tags are metadata used for discovery/filtering, not the source of truth for metric writes or reads.
