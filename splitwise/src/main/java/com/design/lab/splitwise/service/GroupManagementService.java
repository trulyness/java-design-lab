package com.design.lab.splitwise.service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.design.lab.splitwise.model.Group;
import com.design.lab.splitwise.store.Store;

public class GroupManagementService {
    private final Store store;

    public GroupManagementService(final Store store) {
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

    public void addMembersToGroup(final String groupId, final Set<String> members) {
        this.store.addMembersToGroup(groupId, members);
    }

    public void removeMembersFromGroup(final String groupId, final Set<String> members) {
        this.store.removeMembersFromGroup(groupId, members);
    }
}
