package com.courseapp.dto;

import java.util.Map;

public class AuthResponse {
    private String token;
    private Map<String, Object> user;

    public AuthResponse(String token, Map<String, Object> user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public Map<String, Object> getUser() { return user; }
}