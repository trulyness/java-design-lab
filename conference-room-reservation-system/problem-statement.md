# Multi-Building Conference Room Reservation System

Design and implement a thread safe in-memory conference room reservation system for a company with multiple office buildings.

Employees should be able to search and reserve conference rooms across buildings based on time, capacity, and room features. The system should prevent double-booking, handle concurrent reservation requests, and support reservation cancellation.

You are expected to write clean, executable code with proper classes, abstractions, and tests.

## Functional Requirements

1. Buildings and rooms

The system has multiple buildings. Each building has multiple conference rooms.

Each building has:

- buildingId
- name
- location

Each room has:

- roomId
- buildingId
- capacity
- supported features:
  - WHITEBOARD
  - PROJECTOR
  - VIDEO_CONFERENCE
  - PHONE
- booking calendar

Assume buildings and rooms are preconfigured at system startup.

2. Reserve a room

The system should:

- find an available room matching the requirements
- reserve it if available
- return a reservation object containing:
    - reservationId
    - roomId
    - buildingId
    - employeeId
    - startTime
    - endTime
    - status

If no matching room is available, return an error or throw a custom exception.

3. Room availability check

- This should return true if the room has no overlapping confirmed reservation.
- Use half-open interval semantics. 

4. Cancel reservation

5. List reservations for employee

Return all non-cancelled reservations for that employee sorted by start time.

## Concurrency Requirements

Multiple employees may concurrently:

- reserve rooms
- cancel reservations
- query availability
- list reservations

The system must ensure:

- no room is double-booked
- cancellation racing with reservation is handled safely
- reservation IDs are unique
- reads do not observe corrupted state