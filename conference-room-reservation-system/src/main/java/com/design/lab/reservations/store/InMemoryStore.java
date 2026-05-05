package com.design.lab.reservations.store;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.design.lab.reservations.enums.RoomFeature;
import com.design.lab.reservations.enums.Status;
import com.design.lab.reservations.exception.BuildingNotFoundException;
import com.design.lab.reservations.exception.EmployeeNotFoundException;
import com.design.lab.reservations.exception.InvalidIntervalException;
import com.design.lab.reservations.exception.NoRoomsAvailableException;
import com.design.lab.reservations.exception.ReservationCannotBeCancelledException;
import com.design.lab.reservations.exception.ReservationNotFoundException;
import com.design.lab.reservations.model.Building;
import com.design.lab.reservations.model.Employee;
import com.design.lab.reservations.model.Reservation;
import com.design.lab.reservations.model.Room;

public class InMemoryStore implements Store {
    private final Map<String, Map<Integer, List<Room>>> buildingMap;
    private final Map<String, NavigableMap<Instant, List<Reservation>>> reservationsByEmployeesMap;
    private final Map<String, Reservation> reservationMap;
    private final Map<String, Room> roomMap;
    private final Map<String, Employee> employeeMap;

    public InMemoryStore(final Set<Building> buildings, final Set<Room> rooms, final Set<Employee> employees) {
        final Map<String, Map<Integer, List<Room>>> mutableBuildingMap = new ConcurrentHashMap<>();
        this.reservationsByEmployeesMap = new ConcurrentHashMap<>();
        this.reservationMap = new ConcurrentHashMap<>();
        this.roomMap = new ConcurrentHashMap<>();
        this.employeeMap = new ConcurrentHashMap<>();

        for (final Employee employee: employees) {
            employeeMap.put(employee.getEmployeeId(),employee);
        }
        
        for (final Building building: buildings) {
            mutableBuildingMap.put(building.getBuildingId(), new ConcurrentHashMap<>());
        }

        for (final Room room: rooms) {
            mutableBuildingMap.get(room.getBuildingId())
                .computeIfAbsent(room.getFloor(),k -> new ArrayList<>())
                .add(room);

            this.roomMap.put(room.getRoomId(),room);
        }

        final Map<String, Map<Integer, List<Room>>> immutableBuildingMap = new ConcurrentHashMap<>();
        for (final Map.Entry<String, Map<Integer, List<Room>>> buildingEntry : mutableBuildingMap.entrySet()) {
            final Map<Integer, List<Room>> immutableFloorMap = new ConcurrentHashMap<>();
            for (final Map.Entry<Integer, List<Room>> floorEntry : buildingEntry.getValue().entrySet()) {
                immutableFloorMap.put(floorEntry.getKey(), List.copyOf(floorEntry.getValue()));
            }
            immutableBuildingMap.put(buildingEntry.getKey(), Map.copyOf(immutableFloorMap));
        }
        this.buildingMap = Map.copyOf(immutableBuildingMap);
    }

    @Override
    public Reservation reserveRoom(final String employeeId,
                                   final int requiredCapacity,
                                   final Instant startTime,
                                   final Instant endTime,
                                   final String buildingId,
                                   final Optional<Set<RoomFeature>> requiredFeatures,
                                   final Optional<Integer> preferredFloor) {
        if (employeeMap.get(employeeId)==null) {
            throw new EmployeeNotFoundException();
        }
        if (!startTime.isBefore(endTime)) {
            throw new InvalidIntervalException();
        }

        final Map<Integer, List<Room>> floorMap = buildingMap.get(buildingId);
        if (floorMap==null) {
            throw new BuildingNotFoundException(buildingId);
        }
        
        if (preferredFloor.isPresent()) {
            final List<Room> preferredRoomList = getFilteredRooms(
                floorMap.get(preferredFloor.get()),
                requiredCapacity,
                requiredFeatures
            );
            Optional<Reservation> reservation = bookRoomFromList(preferredRoomList,startTime,endTime,employeeId);
            if (reservation.isPresent()) return reservation.get();
        }

        final List<Room> otherRooms = getFilteredRooms(
            floorMap.entrySet().stream()
                   .filter(entry -> preferredFloor.map(f -> !f.equals(entry.getKey())).orElse(true))
                   .map(Map.Entry::getValue)
                   .flatMap(List::stream)
                   .toList(),
            requiredCapacity,
            requiredFeatures
        );

        return bookRoomFromList(otherRooms, startTime, endTime, employeeId)
                .orElseThrow(() -> new NoRoomsAvailableException());
    }

    @Override
    public void cancelReservation(final String reservationId, final String employeeId) {
        final Reservation reservation = reservationMap.get(reservationId);
        if (reservation == null) {
            throw new ReservationNotFoundException();
        }
        if (!reservation.getEmployeeId().equals(employeeId)) {
            throw new ReservationCannotBeCancelledException();
        }
        final Instant startTime = reservation.getStartTime();
        final Room room = roomMap.get(reservation.getRoomId());
        room.cancelReservation(startTime);

    }

    private Optional<Reservation> bookRoomFromList(final List<Room> rooms, 
                                                   final Instant start,
                                                   final Instant end,
                                                   final String employeeId) {
        if (rooms == null) return Optional.empty();

        for (final Room room : rooms) {
            final Optional<Reservation> reservationOptional = room.bookRoomIfAvailable(start, end, employeeId);
            if (reservationOptional.isPresent()) {
                final Reservation reservation = reservationOptional.get();
                reservationMap.put(reservation.getReservationId(),reservation);
                reservationsByEmployeesMap.computeIfAbsent(employeeId, k -> new ConcurrentSkipListMap<>())
                                          .computeIfAbsent(reservation.getStartTime(), k -> new CopyOnWriteArrayList<>())
                                          .add(reservation);
                return reservationOptional;
            }
        }

        return Optional.empty();
    }

    private List<Room> getFilteredRooms(final Collection<Room> rooms,
                                        final int requiredCapacity,
                                        final Optional<Set<RoomFeature>> requiredFeatures) {
        if (rooms == null) return new ArrayList<>();
        return rooms.stream()
                    .filter(room -> room.getCapacity() >= requiredCapacity)
                    .filter(room -> requiredFeatures.map(features -> room.getSupportedFeatures().containsAll(features))
                                                    .orElse(true))
                    .toList();
    }

    @Override
    public List<Reservation> listReservationForEmployee(final String employeeId) {
        if (employeeMap.get(employeeId)==null) {
            throw new EmployeeNotFoundException();
        }
        final NavigableMap<Instant, List<Reservation>> reservations = reservationsByEmployeesMap.get(employeeId);
        if (reservations==null) return new ArrayList<>();
        return reservations.values().stream()
                .flatMap(List::stream)
                .filter(reservation -> !reservation.getStatus().equals(Status.CANCELLED))
                .toList();
    }
    
}
