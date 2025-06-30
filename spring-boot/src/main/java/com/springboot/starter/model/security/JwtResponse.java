package com.springboot.starter.model.security;

public record JwtResponse(
    String token,
    String type
) {
    public JwtResponse(String token) {
        this(token, "Bearer");
    }
}
