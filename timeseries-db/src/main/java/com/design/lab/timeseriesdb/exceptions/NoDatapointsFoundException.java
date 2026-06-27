package com.design.lab.timeseriesdb.exceptions;

public class NoDatapointsFoundException extends RuntimeException {
    public NoDatapointsFoundException() {
        super("No datapoints found!");
    }
}
