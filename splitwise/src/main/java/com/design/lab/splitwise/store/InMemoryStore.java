package com.design.lab.splitwise.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.design.lab.splitwise.exceptions.GroupNotFoundException;
import com.design.lab.splitwise.exceptions.UserAlreadyExistsException;
import com.design.lab.splitwise.exceptions.UserNotFoundException;
import com.design.lab.splitwise.model.Group;
import com.design.lab.splitwise.model.User;

public class InMemoryStore implements Store {
    private final Map<String, User> users;
    private final Map<String, Group> groups;

    public InMemoryStore() {
        this.users = new HashMap<>();
        this.groups = new HashMap<>();
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
        groups.put(group.getGroupId(), group);
    }

    @Override
    public void addMembersToGroup(final String groupId, final Set<String> members) {
        final Group group = groups.get(groupId);
        if (group == null) {
            throw new GroupNotFoundException();
        }
        group.addMembers(members);
    }

    @Override
    public void removeMembersFromGroup(final String groupId, final Set<String> newMembers) {
        final Group group = groups.get(groupId);
        if (group == null) {
            throw new GroupNotFoundException();
        }

        group.removeMembers(newMembers);
    }

}
