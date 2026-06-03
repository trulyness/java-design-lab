package com.design.lab.splitwise.model;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class Expense {
    private final String expenseId;
    private final String paidBy;
    private final String description;
    private final Set<String> participants;
}
