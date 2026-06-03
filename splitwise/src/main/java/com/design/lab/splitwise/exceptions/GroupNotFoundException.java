package com.design.lab.splitwise.exceptions;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException() {
        super("Group not found!");
    }
}
