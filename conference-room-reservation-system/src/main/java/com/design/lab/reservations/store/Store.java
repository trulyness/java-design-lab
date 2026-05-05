package com.design.lab.reservations.store;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.design.lab.reservations.enums.RoomFeature;
import com.design.lab.reservations.model.Reservation;

public interface Store {
    Reservation reserveRoom(final String employeeId, 
                            final int requiredCapacity,
                            final Instant startTime,
                            final Instant endTime,
                            final String buildingId,
                            final Optional<Set<RoomFeature>> requiredFeatures,
                            final Optional<Integer> preferredFloor);
    
    void cancelReservation(final String reservationId, final String employeeId);

    List<Reservation> listReservationForEmployee(final String employeeId);
}
