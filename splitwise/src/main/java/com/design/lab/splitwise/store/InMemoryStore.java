package com.design.lab.splitwise.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.design.lab.splitwise.exceptions.GroupMembershipRequiredException;
import com.design.lab.splitwise.exceptions.GroupNotFoundException;
import com.design.lab.splitwise.exceptions.UserAlreadyExistsException;
import com.design.lab.splitwise.exceptions.UserNotFoundException;
import com.design.lab.splitwise.model.Expense;
import com.design.lab.splitwise.model.Group;
import com.design.lab.splitwise.model.User;

public class InMemoryStore implements Store {
    private final Map<String, User> users;
    private final Map<String, Group> groups;
    private final Map<String, Expense> expenses;

    public InMemoryStore() {
        this.users = new HashMap<>();
        this.groups = new HashMap<>();
        this.expenses = new HashMap<>();
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
        return expense.getExpenseId();
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
