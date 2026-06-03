package com.design.lab.splitwise.store;

import java.util.Set;

import com.design.lab.splitwise.model.Group;
import com.design.lab.splitwise.model.User;

public interface Store {
    void createUser(final User user);
    void updateUser(final User user);

    void createGroup(final Group group);
    void addMembersToGroup(final String authenticatedEmail, final String groupId, final Set<String> members);
    void removeMembersFromGroup(final String authenticatedEmail, final String groupId, final Set<String> newMembers);
}
