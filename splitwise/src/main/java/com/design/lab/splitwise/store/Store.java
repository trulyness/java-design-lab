package com.design.lab.splitwise.store;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.design.lab.splitwise.model.Expense;
import com.design.lab.splitwise.model.Group;
import com.design.lab.splitwise.model.User;

public interface Store {
    void createUser(final User user);
    void updateUser(final User user);

    void createGroup(final Group group);
    void addMembersToGroup(final String authenticatedEmail, final String groupId, final Set<String> members);
    void removeMembersFromGroup(final String authenticatedEmail, final String groupId, final Set<String> newMembers);
    Set<String> getParticipantsInGroup(final String authenticatedEmail, final String groupId);
    List<Expense> getGroupExpenses(final String authenticatedEmail, final String groupId);

    String createExpense(final Expense expense);

    Map<String, BigDecimal> getBalancesForUser(final String authenticatedEmail);
}
