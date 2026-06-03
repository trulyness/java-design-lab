package com.design.lab.splitwise.model;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class Group {
    private final String groupId;
    private final String name;
    private final String description;
    private final Set<String> members;

    public void addMembers(final Set<String> newMembers) {
        this.members.addAll(newMembers);
    }

    public void removeMembers(final Set<String> newMembers) {
        this.members.removeAll(newMembers);
    }
}
