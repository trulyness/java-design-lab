package com.design.lab.splitwise.service;

import java.math.BigDecimal;
import java.util.Map;

import com.design.lab.splitwise.store.Store;

public class BalanceService {
    private final Store store;

    public BalanceService(final Store store) {
        this.store = store;
    }

    public Map<String, BigDecimal> getBalancesForUser(final String authenticatedEmail) {
        return this.store.getBalancesForUser(authenticatedEmail);
    }

    public void settleUp(final String authenticatedEmail,
                    final String paidTo,
                    final BigDecimal amount) {
        this.store.settleUp(authenticatedEmail, paidTo, amount);
    }
}
