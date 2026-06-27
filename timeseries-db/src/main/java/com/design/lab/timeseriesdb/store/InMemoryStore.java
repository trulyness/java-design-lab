package com.design.lab.timeseriesdb.store;

import com.design.lab.timeseriesdb.enums.AggregationType;
import com.design.lab.timeseriesdb.exceptions.InvalidBucketSizeException;
import com.design.lab.timeseriesdb.exceptions.InvalidRangeException;
import com.design.lab.timeseriesdb.exceptions.MetricAlreadyExistsException;
import com.design.lab.timeseriesdb.exceptions.MetricNotFoundException;
import com.design.lab.timeseriesdb.exceptions.NoDatapointsFoundException;
import com.design.lab.timeseriesdb.model.Datapoint;
import com.design.lab.timeseriesdb.model.Metric;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.DoubleStream;

public class InMemoryStore {
    private final Map<String, MetricState> registeredMetrics;
    private final Map<String, Map<String, Set<String>>> tagsToMetric;

    public InMemoryStore() {
        this.registeredMetrics = new ConcurrentHashMap<>();
        this.tagsToMetric = new ConcurrentHashMap<>();
    }

    public void registerMetric(final Metric metric) {
        final MetricState metricState = MetricState.builder()
                                                    .metric(metric)
                                                    .datapoints(new TreeMap<>())
                                                    .lock(new ReentrantReadWriteLock())
                                                    .build();

        if (registeredMetrics.putIfAbsent(metric.getName(), metricState)!=null) {
            throw new MetricAlreadyExistsException(metric.getName());
        }

        for (final Map.Entry<String, String> tag : metric.getTags().entrySet()) {
            this.tagsToMetric
                .computeIfAbsent(tag.getKey(), k -> new ConcurrentHashMap<>())
                .computeIfAbsent(tag.getValue(), v -> ConcurrentHashMap.newKeySet())
                .add(metric.getName());
        }
    }

    public void writeMetricData(final String metricName, final long timestampMillis, final double value) { 
        final MetricState metricState = registeredMetrics.get(metricName);
        if (metricState==null) {
            throw new MetricNotFoundException(metricName);
        }

        metricState.getLock().writeLock().lock();
        try {
            final Datapoint datapoint = Datapoint.builder()
                                            .metricName(metricName)
                                            .timestampMillis(timestampMillis)
                                            .value(value)
                                            .build();

            final NavigableMap<Long, List<Datapoint>> datapoints = metricState.getDatapoints();
            datapoints.computeIfAbsent(timestampMillis, k -> new ArrayList<>()).add(datapoint);
        } finally {
            metricState.getLock().writeLock().unlock();
        }
        
    }

    public List<Datapoint> getMetricData(final String metricName, final long startTimeMillis, final long endTimeMillis) {
        final MetricState metricState = registeredMetrics.get(metricName);
        if (metricState==null) {
            throw new MetricNotFoundException(metricName);
        }

        metricState.getLock().readLock().lock();
        try {

            validateTimeRange(startTimeMillis, endTimeMillis);
   
            final NavigableMap<Long, List<Datapoint>> datapoints = metricState.getDatapoints();
            return datapoints.subMap(startTimeMillis, true, endTimeMillis, true)
                            .values().stream().flatMap(List::stream).toList();
        } finally {
            metricState.getLock().readLock().unlock();
        }
        
    }

    public double aggregateMetricData(final String metricName,
                                    final long startTimeMillis,
                                    final long endTimeMillis,
                                    final AggregationType aggregationType) {
        
        final List<Datapoint> currDatapoints = getMetricData(metricName, startTimeMillis, endTimeMillis);

        return aggregateDatapoints(currDatapoints, aggregationType);
    }

    private double aggregateDatapoints(final List<Datapoint> currDatapoints,
                                    final AggregationType aggregationType) {
        if (currDatapoints.isEmpty()) {
            throw new NoDatapointsFoundException();
        }

        return switch (aggregationType) {
            case COUNT -> currDatapoints.size();
            case SUM -> valuesOf(currDatapoints).sum();
            case AVG -> valuesOf(currDatapoints).average().getAsDouble();
            case MIN -> valuesOf(currDatapoints).min().getAsDouble();
            case MAX -> valuesOf(currDatapoints).max().getAsDouble();
        };
    }

    public Optional<Datapoint> getLatestMetricData(final String metricName) {
        final MetricState metricState = registeredMetrics.get(metricName);
        if (metricState==null) {
            throw new MetricNotFoundException(metricName);
        }

        metricState.getLock().readLock().lock();
        try {
            final NavigableMap<Long, List<Datapoint>> metricDatapoints = metricState.getDatapoints();

            if (metricDatapoints.isEmpty()) {
                return Optional.empty();
            }

            final List<Datapoint> latestTimestampDatapoints = metricDatapoints.lastEntry().getValue();

            return Optional.of(latestTimestampDatapoints.get(latestTimestampDatapoints.size() - 1));
        } finally {
            metricState.getLock().readLock().unlock();
        }
    }

    public Map<Long, Double> aggregateByBucket(final String metricName,
                            final long startTimeMillis,
                            final long endTimeMillis,
                            final long bucketSizeMillis,
                            final AggregationType aggregationType) {
        validateTimeRange(startTimeMillis, endTimeMillis);
        if (bucketSizeMillis <= 0) {
            throw new InvalidBucketSizeException();
        }

        final Map<Long, Double> result = new TreeMap<>();

        for (long i=startTimeMillis;i<=endTimeMillis;i+=bucketSizeMillis) {
            final long bucketEndTimeMillis = Math.min(i + bucketSizeMillis - 1, endTimeMillis);
            final List<Datapoint> bucketDatapoints = getMetricData(metricName, i, bucketEndTimeMillis);

            if (!bucketDatapoints.isEmpty()) {
                result.put(i, aggregateDatapoints(bucketDatapoints, aggregationType));
            }
        }

        return result;
    }

    private DoubleStream valuesOf(final List<Datapoint> datapoints) {
        return datapoints.stream().mapToDouble(Datapoint::getValue);
    }

    private void validateTimeRange(final long startTimeMillis, final long endTimeMillis) {
        if (startTimeMillis > endTimeMillis) {
            throw new InvalidRangeException();
        }
    }

    public List<Metric> findMetricsByTags(final Map<String, String> tags) {
        if (tags.isEmpty()) {
            return registeredMetrics.values()
                    .stream()
                    .map(MetricState::getMetric)
                    .toList();
        }

        Set<String> matchingMetricNames = null;

        for (final Map.Entry<String, String> tag : tags.entrySet()) {
            final Map<String, Set<String>> valuesToMetrics = tagsToMetric.get(tag.getKey());
            if (valuesToMetrics == null) {
                return List.of();
            }

            final Set<String> metricNames = valuesToMetrics.get(tag.getValue());
            if (metricNames == null) {
                return List.of();
            }

            if (matchingMetricNames == null) {
                matchingMetricNames = new HashSet<>(metricNames);
            } else {
                matchingMetricNames.retainAll(metricNames);
            }
        }

        return matchingMetricNames.stream()
                .map(registeredMetrics::get)
                .filter(Objects::nonNull)
                .map(MetricState::getMetric)
                .toList();
    }

}
