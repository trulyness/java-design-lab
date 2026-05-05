package com.design.lab.reservations.model;

import com.design.lab.reservations.enums.RoomFeature;
import com.design.lab.reservations.enums.Status;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Room {
    private final String roomId;
    private final String buildingId;
    private final int capacity;
    private final int floor;
    private final Set<RoomFeature> supportedFeatures;
    private final ReentrantLock lock = new ReentrantLock();
    @Setter
    private TreeMap<Instant,Reservation> bookingCalendar;

    @Builder
    public Room(final String roomId,
                final String buildingId,
                final int capacity,
                final int floor,
                final Set<RoomFeature> supportedFeatures) {
        this.roomId = roomId;
        this.buildingId = buildingId;
        this.capacity = capacity;
        this.floor = floor;
        this.supportedFeatures = supportedFeatures;
        this.bookingCalendar = new TreeMap<>();
    }

    public boolean isRoomAvailable(final Instant startTime, final Instant endTime) {
        lock.lock();
        try {
            final Map.Entry<Instant, Reservation> prev = bookingCalendar.floorEntry(startTime);
            if (prev != null && prev.getValue().getEndTime().isAfter(startTime)) {
                return false;
            }
            final Map.Entry<Instant, Reservation> next = bookingCalendar.ceilingEntry(startTime);
            if (next != null && next.getKey().isBefore(endTime)) {
                return false;
            }
            return true;
        } finally {
            lock.unlock();
        }
        
    }

    public Optional<Reservation> bookRoomIfAvailable(final Instant startTime, final Instant endTime, final String employeeId) {
        lock.lock();
        try {
            if (!isRoomAvailable(startTime, endTime)) {
                return Optional.empty();
            }
            final Reservation reservation = Reservation.builder()
                .reservationId(UUID.randomUUID().toString())
                .roomId(roomId)
                .buildingId(buildingId)
                .startTime(startTime)
                .endTime(endTime)
                .status(Status.RESERVED)
                .employeeId(employeeId)
                .build();
            bookingCalendar.put(startTime,reservation);
            return Optional.of(reservation);
        } finally {
            lock.unlock();
        }
        
    }

    public void cancelReservation(final Instant startTime) {
        lock.lock();
        try {
            final Reservation reservation = bookingCalendar.remove(startTime);
            if (reservation != null) {
                reservation.setStatus(Status.CANCELLED);
            }

        } finally {
            lock.unlock();
        }
        
    }

}
