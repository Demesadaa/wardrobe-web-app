package com.wardrobe.user;

public enum UserRole {
    USER,
    ADMIN;

    public String securityName() {
        return name();
    }
}
