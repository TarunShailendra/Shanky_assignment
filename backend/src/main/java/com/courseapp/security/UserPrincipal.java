package com.courseapp.security;

public class UserPrincipal {
    private final Long userId;
    private final String email;
    private final String role;

    public UserPrincipal(Long userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public Long userId() { return userId; }
    public String email() { return email; }
    public String role() { return role; }
}