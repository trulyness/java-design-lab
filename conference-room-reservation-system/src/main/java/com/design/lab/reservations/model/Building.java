package com.design.lab.reservations.model;

import com.design.lab.reservations.enums.Location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class Building {
    private final String buildingId;
    private final String name;
    private final Location location;
}
