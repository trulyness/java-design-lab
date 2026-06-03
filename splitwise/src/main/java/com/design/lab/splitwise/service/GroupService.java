package com.design.lab.splitwise.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.design.lab.splitwise.model.Expense;
import com.design.lab.splitwise.model.Group;
import com.design.lab.splitwise.store.Store;

public class GroupService {
    private final Store store;

    public GroupService(final Store store) {
        this.store = store;
    }

    public String createGroup(final String name, final String description, final Set<String> members) {
        final Group group = Group.builder()
                            .groupId(UUID.randomUUID().toString())
                            .name(name)
                            .description(description)
                            .members(new HashSet<>(members))
                            .build();
        this.store.createGroup(group);
        return group.getGroupId();
    }

    public void addMembersToGroup(final String authenticatedEmail,
                                  final String groupId,
                                  final Set<String> members) {
        this.store.addMembersToGroup(authenticatedEmail, groupId, members);
    }

    public void removeMembersFromGroup(final String authenticatedEmail,
                                       final String groupId,
                                       final Set<String> members) {
        this.store.removeMembersFromGroup(authenticatedEmail, groupId, members);
    }

    public Set<String> getParticipantsInGroup(final String authenticatedEmail,
                                              final String groupId) {
        return this.store.getParticipantsInGroup(authenticatedEmail, groupId);
    }

    public List<Expense> getGroupExpenses(final String authenticatedEmail,
                                          final String groupId) {
        return this.store.getGroupExpenses(authenticatedEmail, groupId);
    }
}
