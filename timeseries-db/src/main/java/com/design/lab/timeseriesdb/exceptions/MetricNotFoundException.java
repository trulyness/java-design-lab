package com.design.lab.timeseriesdb.exceptions;

public class MetricNotFoundException extends RuntimeException {
    public MetricNotFoundException(final String name) {
        super("Metric " + name + " was not found!");
    }
}
