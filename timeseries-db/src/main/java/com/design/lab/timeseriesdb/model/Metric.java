package com.design.lab.timeseriesdb.model;

import com.design.lab.timeseriesdb.enums.MetricType;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Metric {
    private final String name;
    private final MetricType metricType;
    private final String description;
    private final Map<String, String> tags;
}
