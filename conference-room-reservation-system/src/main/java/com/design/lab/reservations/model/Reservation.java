package com.design.lab.reservations.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import com.design.lab.reservations.enums.Status;

@Builder
@Getter
@AllArgsConstructor
public class Reservation {
    private final String reservationId;
    private final String roomId;
    private final String buildingId;
    private final String employeeId;
    private final Instant startTime;
    private final Instant endTime;
    @Setter
    private Status status;
}
