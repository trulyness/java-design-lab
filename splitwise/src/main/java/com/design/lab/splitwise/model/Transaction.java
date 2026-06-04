package com.design.lab.splitwise.model;

import java.math.BigDecimal;
import java.time.Instant;

import com.design.lab.splitwise.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class Transaction {
    private final String id;
    private final TransactionType type;
    private final Instant createdAt;
    private final BigDecimal amount;
    private final String paidBy;
    private final String paidTo;
    private final String groupId;
}
