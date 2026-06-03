package com.design.lab.splitwise.exceptions;

public class GroupMembershipRequiredException extends RuntimeException {
    public GroupMembershipRequiredException(final String email, final String groupId) {
        super("User with email " + email + " is not a member of group " + groupId + "!");
    }
}
