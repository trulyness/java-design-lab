package com.design.lab.reservations.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class Employee {
    private final String employeeId;
}
