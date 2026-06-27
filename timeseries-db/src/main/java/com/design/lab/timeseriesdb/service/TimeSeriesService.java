package com.design.lab.timeseriesdb.service;

import com.design.lab.timeseriesdb.enums.AggregationType;
import com.design.lab.timeseriesdb.enums.MetricType;
import com.design.lab.timeseriesdb.model.Datapoint;
import com.design.lab.timeseriesdb.model.Metric;
import com.design.lab.timeseriesdb.store.InMemoryStore;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TimeSeriesService {

    private final InMemoryStore store;

    public TimeSeriesService(final InMemoryStore store) {
        this.store = store;
    }

    public void registerMetric(final String metricName, final MetricType metricType, final String description) {
        registerMetric(metricName, metricType, description, Collections.emptyMap());
    }

    public void registerMetric(final String metricName,
                            final MetricType metricType,
                            final String description,
                            final Map<String, String> tags) {
        final Metric metric = Metric.builder()
                                    .name(metricName)
                                    .metricType(metricType)
                                    .description(description)
                                    .tags(Map.copyOf(tags))
                                    .build();

        this.store.registerMetric(metric);
    }

    public void writeMetricData(final String metricName, final long timestampMillis, final double value) {
        this.store.writeMetricData(metricName, timestampMillis, value);
    }

    public List<Datapoint> getMetricData(final String metricName, final long startTimeMillis, final long endTimeMillis) {
        return this.store.getMetricData(metricName, startTimeMillis, endTimeMillis);
    }

    public double aggregateMetricData(final String metricName, final long startTimeMillis, final long endTimeMillis, final AggregationType aggregationType) {
        return this.store.aggregateMetricData(metricName, startTimeMillis, endTimeMillis, aggregationType);
    }

    public Optional<Datapoint> getLatestMetricData(final String metricName) {
        return this.store.getLatestMetricData(metricName);
    }

    public Map<Long, Double> aggregateByBucket(final String metricName,
                                            final long startTimeMillis,
                                            final long endTimeMillis,
                                            final long bucketSizeMillis,
                                            final AggregationType aggregationType) {
        return this.store.aggregateByBucket(metricName, startTimeMillis, endTimeMillis, bucketSizeMillis, aggregationType);
    }

    public List<Metric> findMetricsByTags(final Map<String, String> tags) {
        return this.store.findMetricsByTags(tags);
    }
}
