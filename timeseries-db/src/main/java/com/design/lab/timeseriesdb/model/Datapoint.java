package com.design.lab.timeseriesdb.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Datapoint {
    final String metricName;
    final long timestampMillis;
    final double value;
}
