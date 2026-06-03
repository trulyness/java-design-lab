package com.design.lab.trading.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@Getter
@Builder
@ToString
@EqualsAndHashCode(of = "userId")
public class User {

    private final String userId;
    private final String userName;
    private final String phoneNumber;
    private final String emailId;
}