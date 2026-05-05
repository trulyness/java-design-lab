package com.design.lab.reservations.exception;

public class BuildingNotFoundException extends RuntimeException {
    public BuildingNotFoundException(final String buildingId) {
        super("Building " + buildingId + " was not found!");
    }
}
