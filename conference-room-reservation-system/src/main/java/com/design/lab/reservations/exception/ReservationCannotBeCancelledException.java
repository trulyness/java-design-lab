package com.design.lab.reservations.exception;

public class ReservationCannotBeCancelledException extends RuntimeException {
    public ReservationCannotBeCancelledException() {
        super("Reservation cannot be cancelled!");
    }
}
