package com.design.lab.splitwise.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import com.design.lab.splitwise.enums.SplitType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class Expense {
    private final String expenseId;
    private final String paidBy;
    private final String createdBy;
    private final Instant createdAt;
    private final String description;
    private final SplitType splitType;
    private final Map<String, BigDecimal> participantShares;
    private final String groupId;
    private final BigDecimal amount;
}
