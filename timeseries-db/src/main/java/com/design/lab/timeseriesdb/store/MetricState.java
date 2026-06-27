package com.design.lab.timeseriesdb.store;

import com.design.lab.timeseriesdb.model.Datapoint;
import com.design.lab.timeseriesdb.model.Metric;

import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MetricState {
    private final Metric metric;
    private final NavigableMap<Long, List<Datapoint>> datapoints;
    private final ReentrantReadWriteLock lock;
}
