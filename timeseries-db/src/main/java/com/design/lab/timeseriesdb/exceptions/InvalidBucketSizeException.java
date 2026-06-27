package com.design.lab.timeseriesdb.exceptions;

public class InvalidBucketSizeException extends RuntimeException {
    public InvalidBucketSizeException() {
        super("bucket size must be greater than 0");
    }
}
