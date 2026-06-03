package com.design.lab.splitwise.service;

import com.design.lab.splitwise.model.User;
import com.design.lab.splitwise.store.Store;

public class ProfileManagementService {
    private final Store store;

    public ProfileManagementService(Store store) {
        this.store = store;
    }

    public void createUserProfile(final String authenticatedEmail, final String name, final String phoneNumber) {
        final User user = User.builder()
                              .email(authenticatedEmail)
                              .name(name)
                              .phoneNumber(phoneNumber)
                              .build();
        this.store.createUser(user);
    }

    public void updateUserProfile(final String authenticatedEmail, final String name, final String phoneNumber) {
        final User user = User.builder()
                              .email(authenticatedEmail)
                              .name(name)
                              .phoneNumber(phoneNumber)
                              .build();
        this.store.updateUser(user);
    }
    
}
