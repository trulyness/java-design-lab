package com.design.lab.splitwise.store;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.design.lab.splitwise.enums.TransactionType;
import com.design.lab.splitwise.exceptions.GroupMembershipRequiredException;
import com.design.lab.splitwise.exceptions.GroupNotFoundException;
import com.design.lab.splitwise.exceptions.InvalidExpenseException;
import com.design.lab.splitwise.exceptions.UserAlreadyExistsException;
import com.design.lab.splitwise.exceptions.UserNotFoundException;
import com.design.lab.splitwise.model.Expense;
import com.design.lab.splitwise.model.Group;
import com.design.lab.splitwise.model.Transaction;
import com.design.lab.splitwise.model.User;

public class InMemoryStore implements Store {
    private final Map<String, User> users;
    private final Map<String, Group> groups;
    private final Map<String, Expense> expenses;
    private final Map<String, List<String>> expensesPerGroup;
    private final Map<String, Map<String, BigDecimal>> balances; 
    private final Map<String, Transaction> transactions;
    private final Map<String, List<String>> transactionsPerUser;

    public InMemoryStore() {
        this.users = new HashMap<>();
        this.groups = new HashMap<>();
        this.expenses = new HashMap<>();
        this.expensesPerGroup = new HashMap<>();
        this.balances = new HashMap<>();
        this.transactions = new HashMap<>();
        this.transactionsPerUser = new HashMap<>();
    }

    @Override
    public void createUser(final User user) {
        final String email = user.getEmail();
        if (users.containsKey(email)) {
            throw new UserAlreadyExistsException(email);
        }
        users.put(email, user);
    }

    @Override
    public void updateUser(final User user) {
        final String email = user.getEmail();
        if (!users.containsKey(email)) {
            throw new UserNotFoundException(email);
        }

        users.put(email, user);
    }

    @Override
    public void createGroup(final Group group) {
        validateUsersExist(group.getMembers());
        groups.put(group.getGroupId(), group);
    }

    @Override
    public void addMembersToGroup(final String authenticatedEmail,
                                  final String groupId,
                                  final Set<String> members) {
        final Group group = getGroupForMember(authenticatedEmail, groupId);
        validateUsersExist(members);
        group.addMembers(members);
    }

    @Override
    public void removeMembersFromGroup(final String authenticatedEmail,
                                       final String groupId,
                                       final Set<String> newMembers) {
        final Group group = getGroupForMember(authenticatedEmail, groupId);
        group.removeMembers(newMembers);
    }

    @Override
    public Set<String> getParticipantsInGroup(final String authenticatedEmail, final String groupId) {
        final Group group = getGroupForMember(authenticatedEmail, groupId);
        return group.getMembers();
    }

    @Override
    public String createExpense(final Expense expense) {
        final String authenticatedEmail = expense.getCreatedBy();
        final String groupId = expense.getGroupId();
        final Group group = getGroupForMember(authenticatedEmail, groupId);
        final Set<String> participants = expense.getParticipantShares().keySet();

        validateUserInGroup(expense.getPaidBy(), group);
        validateUsersInGroup(participants, group);

        expenses.put(expense.getExpenseId(), expense);
        expensesPerGroup.computeIfAbsent(groupId, k -> new ArrayList<>()).add(expense.getExpenseId());
        final Map<String, BigDecimal> shares = expense.getParticipantShares();
        for (final Map.Entry<String, BigDecimal> entry : shares.entrySet()) {
            final String participant = entry.getKey();
            final BigDecimal share = entry.getValue();
            if (!participant.equals(expense.getPaidBy())) {
                updateBalance(participant, expense.getPaidBy(), share);
                final Transaction transaction = Transaction.builder()
                                                            .id(UUID.randomUUID().toString())
                                                            .type(TransactionType.EXPENSE)
                                                            .amount(share)
                                                            .paidBy(participant)
                                                            .paidTo(expense.getPaidBy())
                                                            .createdAt(Instant.now())
                                                            .groupId(groupId)
                                                            .build();
                transactions.put(transaction.getId(), transaction);
                transactionsPerUser.computeIfAbsent(participant, k -> new ArrayList<>()).add(transaction.getId());
                transactionsPerUser.computeIfAbsent(expense.getPaidBy(), k -> new ArrayList<>()).add(transaction.getId());
            }
        }
        return expense.getExpenseId();
    }

    @Override
    public List<Expense> getGroupExpenses(final String authenticatedEmail, final String groupId) {
        getGroupForMember(authenticatedEmail, groupId);
        final List<String> expenseIds = expensesPerGroup.getOrDefault(groupId, new ArrayList<>());
        final List<Expense> expensesForGroup = new ArrayList<>();
        for (final String id : expenseIds) {
            expensesForGroup.add(expenses.get(id));
        }
        return expensesForGroup;
    }

    @Override
    public Map<String, BigDecimal> getBalancesForUser(final String authenticatedEmail) {
        if (!users.containsKey(authenticatedEmail)) {
            throw new UserNotFoundException(authenticatedEmail);
        }

        return new HashMap<>(balances.getOrDefault(authenticatedEmail, new HashMap<>()));
    }

    @Override
    public void settleUp(final String authenticatedEmail, final String paidTo, final BigDecimal amount) {
        if (!users.containsKey(authenticatedEmail)) {
            throw new UserNotFoundException(authenticatedEmail);
        }

        if (!users.containsKey(paidTo)) {
            throw new UserNotFoundException(paidTo);
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidExpenseException("Settlement amount must be greater than zero");
        }

        updateBalance(paidTo, authenticatedEmail, amount);
        final Transaction transaction = Transaction.builder()
                                                    .id(UUID.randomUUID().toString())
                                                    .type(TransactionType.SETTLE)
                                                    .amount(amount)
                                                    .paidBy(authenticatedEmail)
                                                    .paidTo(paidTo)
                                                    .createdAt(Instant.now())
                                                    .build();

        transactions.put(transaction.getId(), transaction);
        transactionsPerUser.computeIfAbsent(paidTo, k -> new ArrayList<>()).add(transaction.getId());
        transactionsPerUser.computeIfAbsent(authenticatedEmail, k -> new ArrayList<>()).add(transaction.getId());
    }

    @Override
    public List<Transaction> getTransactionHistory(final String authenticatedEmail) {
        if (!users.containsKey(authenticatedEmail)) {
            throw new UserNotFoundException(authenticatedEmail);
        }

        final List<String> transactionIds = transactionsPerUser.getOrDefault(authenticatedEmail, new ArrayList<>());
        final List<Transaction> transactionsForUser = new ArrayList<>();

        for (final String id : transactionIds) {
            transactionsForUser.add(transactions.get(id));
        }

        return transactionsForUser;
    }

    private void updateBalance(final String paidFor, final String paidBy, final BigDecimal amount) {
        final Map<String, BigDecimal> paidForBalances = balances.computeIfAbsent(paidFor, k -> new HashMap<>());
        final Map<String, BigDecimal> paidByBalances = balances.computeIfAbsent(paidBy, k -> new HashMap<>());

        paidForBalances.put(paidBy, paidForBalances.getOrDefault(paidBy, BigDecimal.ZERO).subtract(amount));
        paidByBalances.put(paidFor, paidByBalances.getOrDefault(paidFor, BigDecimal.ZERO).add(amount));
    }

    private Group getGroupForMember(final String authenticatedEmail, final String groupId) {
        if (!users.containsKey(authenticatedEmail)) {
            throw new UserNotFoundException(authenticatedEmail);
        }

        final Group group = groups.get(groupId);
        if (group == null) {
            throw new GroupNotFoundException();
        }

        if (!group.getMembers().contains(authenticatedEmail)) {
            throw new GroupMembershipRequiredException(authenticatedEmail, groupId);
        }

        return group;
    }

    private void validateUsersExist(final Set<String> members) {
        for (final String member : members) {
            if (!users.containsKey(member)) {
                throw new UserNotFoundException(member);
            }
        }
    }

    private void validateUsersInGroup(final Set<String> members, final Group group) {
        validateUsersExist(members);
        for (final String member : members) {
            validateUserInGroup(member, group);
        }
    }

    private void validateUserInGroup(final String email, final Group group) {
        if (!users.containsKey(email)) {
            throw new UserNotFoundException(email);
        }

        if (!group.getMembers().contains(email)) {
            throw new GroupMembershipRequiredException(email, group.getGroupId());
        }
    }
}
