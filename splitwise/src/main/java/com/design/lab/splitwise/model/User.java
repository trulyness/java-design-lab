package com.design.lab.splitwise.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class User {
    private final String email;
    private final String name;
    private final String phoneNumber;
}
