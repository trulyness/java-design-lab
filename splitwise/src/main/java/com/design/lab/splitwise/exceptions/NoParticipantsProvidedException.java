package com.design.lab.splitwise.exceptions;

public class NoParticipantsProvidedException extends RuntimeException {
    public NoParticipantsProvidedException() {
        super("At least one expense participant must be provided!");
    }
}
