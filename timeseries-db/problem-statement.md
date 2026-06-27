# In-Memory Time Series Metrics Store

Design and implement an efficient in-memory time-series metrics store for a monitoring / observability system.

The system should allow multiple clients to continuously write metric data points and later query aggregated values over time windows.

Your implementation should demonstrate:

- object-oriented design
- good data modeling
- synchronization and concurrency in a multi-threaded environment
- clean abstractions for future extensibility

This is not a full database. It is a lightweight in-memory component.

## Functional Requirements

### 1. Register Metric

A client can register a metric definition.

Each metric has:

- metric name
- metric type
- description (optional)

Supported metric types:

- `COUNTER`
- `GAUGE`

You may assume a metric name is globally unique.

### 2. Write Data Point

A client can write a data point to a metric.

Each data point has:

- metric name
- timestamp (milliseconds)
- value (double)

Examples:

```text
cpu_usage, 1712345600000, 63.5
requests_total, 1712345601000, 5
```

### 3. Query Raw Data Points

A client should be able to fetch all raw data points for a metric in a time range:

```text
query(metricName, startTime, endTime)
```

Return all points with:

- timestamp >= startTime
- timestamp <= endTime

The results should be ordered by timestamp ascending.

### 4. Aggregate Query

A client should be able to query an aggregate for a metric in a time range:

```text
aggregate(metricName, startTime, endTime, aggregationType)
```

Supported aggregations:

- `SUM`
- `AVG`
- `MIN`
- `MAX`
- `COUNT`

### 5. Time Bucket Query

A client should be able to query bucketed aggregations:

```text
aggregateByBucket(metricName, startTime, endTime, bucketSizeMillis, aggregationType)
```

Example:

- metric = `cpu_usage`
- range = last 1 hour
- bucket size = 5 minutes
- aggregation = `AVG`

Return one aggregated value per bucket.

### 6. Latest Value Query

A client should be able to query the latest value for a metric:

```text
getLatest(metricName)
```

If no data exists, return empty / null / optional.

### 7. Concurrency Requirements

The following operations may happen concurrently and must be handled correctly:

- metric registration
- metric writes
- raw queries
- aggregate queries
- latest-value queries

The system should avoid:

- corrupted in-memory state
- inconsistent reads
- lost writes
- duplicate registration of the same metric

## Data To Store

### Metric Definition

- metric name
- metric type
- description

### Data Point

- metric name
- timestamp
- value

## Additional functionality, but not compulsory

Implement if time permits.

### A. Duplicate timestamp policy

Define behavior if the same metric gets multiple writes for the same timestamp:

- overwrite
- keep both
- reject

Document your choice.

### B. Tag support

Metrics may be registered with optional tags. Tags are key-value metadata pairs such as region, host, and service.

The system should support finding metrics by exact tag matches.

```
findMetricsByTags(tags)
```

A metric matches when it contains all requested tag key-value pairs.

```
Example:
tags = { region = us-east-1, service = payments }

matches:
cpu_usage with region=us-east-1, service=payments, host=web-12

does not match:
cpu_usage with region=us-west-2, service=payments
```

### C. Retention policy

Support evicting data older than a configured retention window.

### D. Downsampling

Store precomputed minute-level buckets for faster aggregate queries.