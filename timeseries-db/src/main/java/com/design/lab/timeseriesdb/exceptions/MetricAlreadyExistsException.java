package com.design.lab.timeseriesdb.exceptions;

public class MetricAlreadyExistsException extends RuntimeException {
    public MetricAlreadyExistsException(final String metricName) {
        super("Metric " + metricName + " already exists!");
    }
}
