package com.design.lab.splitwise.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.design.lab.splitwise.enums.SplitType;
import com.design.lab.splitwise.exceptions.InvalidExpenseException;
import com.design.lab.splitwise.exceptions.NoParticipantsProvidedException;
import com.design.lab.splitwise.model.Expense;
import com.design.lab.splitwise.store.Store;

public class ExpenseService {
    private final Store store;

    public ExpenseService(final Store store) {
        this.store = store;
    }

    public String addEqualExpense(final String authenticatedEmail, 
                                  final String groupId,
                                  final String paidBy,
                                  final String description,
                                  final BigDecimal amount,
                                  final Set<String> participants) {
        validatePositiveAmount(amount);

        final Set<String> expenseParticipants = copyParticipants(participants);

        final Map<String, BigDecimal> shares = calculateEqualShares(amount, expenseParticipants);

        final Expense expense = Expense.builder()
                                        .expenseId(UUID.randomUUID().toString())
                                        .splitType(SplitType.EQUAL)
                                        .paidBy(paidBy)
                                        .createdBy(authenticatedEmail)
                                        .groupId(groupId)
                                        .participantShares(shares)
                                        .createdAt(Instant.now())
                                        .amount(amount)
                                        .description(description)
                                        .build();

        this.store.createExpense(expense);
        return expense.getExpenseId();

    }

    public String addPercentageExpense(final String authenticatedEmail, 
                                       final String groupId,
                                       final String paidBy,
                                       final String description,
                                       final BigDecimal amount,
                                       final Map<String, BigDecimal> percentageShares) {

        validatePositiveAmount(amount);

        final Map<String, BigDecimal> percentages = copyShares(percentageShares);

        final Map<String, BigDecimal> shares = new HashMap<>();

        BigDecimal totalPercentage = BigDecimal.ZERO;

        for (final Map.Entry<String, BigDecimal> entry : percentages.entrySet()) {
            final String participant = entry.getKey();
            final BigDecimal share = entry.getValue();

            if (share == null || share.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidExpenseException("Participant shares must be greater than zero");
            }

            final BigDecimal exactShare = amount.multiply(share).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            totalPercentage = totalPercentage.add(share);
            shares.put(participant, exactShare);
        }

        if (totalPercentage.compareTo(new BigDecimal("100")) != 0) {
            throw new InvalidExpenseException("Percentage shares must add up to 100");
        }
        
        final Expense expense = Expense.builder()
                                        .expenseId(UUID.randomUUID().toString())
                                        .splitType(SplitType.PERCENTAGE)
                                        .paidBy(paidBy)
                                        .createdBy(authenticatedEmail)
                                        .groupId(groupId)
                                        .participantShares(shares)
                                        .createdAt(Instant.now())
                                        .amount(amount)
                                        .description(description)
                                        .build();

        this.store.createExpense(expense);
        return expense.getExpenseId();
        
    }

    public String addExactExpense(final String authenticatedEmail, 
                                  final String groupId,
                                  final String paidBy,
                                  final String description,
                                  final BigDecimal amount,
                                  final Map<String, BigDecimal> exactShares) {
        validatePositiveAmount(amount);

        final Map<String, BigDecimal> participantShares = copyShares(exactShares);
        validateExactShares(amount, participantShares);

        final Expense expense = Expense.builder()
                                        .expenseId(UUID.randomUUID().toString())
                                        .splitType(SplitType.EXACT)
                                        .paidBy(paidBy)
                                        .createdBy(authenticatedEmail)
                                        .groupId(groupId)
                                        .participantShares(participantShares)
                                        .createdAt(Instant.now())
                                        .amount(amount)
                                        .description(description)
                                        .build();

        this.store.createExpense(expense);
        return expense.getExpenseId();
    }

    private Set<String> copyParticipants(final Set<String> participants) {
        if (participants == null || participants.isEmpty()) {
            throw new NoParticipantsProvidedException();
        }

        return new HashSet<>(participants);
    }

    private Map<String, BigDecimal> copyShares(final Map<String, BigDecimal> shares) {
        if (shares == null || shares.isEmpty()) {
            throw new NoParticipantsProvidedException();
        }

        return new HashMap<>(shares);
    }

    private void validatePositiveAmount(final BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidExpenseException("Expense amount must be greater than zero");
        }
    }

    private void validateExactShares(final BigDecimal amount, final Map<String, BigDecimal> shares) {
        BigDecimal total = BigDecimal.ZERO;

        for (final BigDecimal share : shares.values()) {
            if (share == null || share.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidExpenseException("Participant shares must be greater than zero");
            }
            total = total.add(share);
        }

        if (total.compareTo(amount) != 0) {
            throw new InvalidExpenseException("Exact shares must add up to the expense amount");
        }
    }

    private Map<String, BigDecimal> calculateEqualShares(final BigDecimal amount,
                                                         final Set<String> participants) {
        final BigDecimal amountPerParticipant = amount.divide(
                BigDecimal.valueOf(participants.size()),
                2,
                RoundingMode.HALF_UP
        );

        final Map<String, BigDecimal> shares = new HashMap<>();
        for (final String participant : participants) {
            shares.put(participant, amountPerParticipant);
        }

        return shares;
    }
}
