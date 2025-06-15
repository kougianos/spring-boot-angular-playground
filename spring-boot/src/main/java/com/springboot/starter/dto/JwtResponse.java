package com.springboot.starter.dto;

public record JwtResponse(
    String token,
    String type
) {
    public JwtResponse(String token) {
        this(token, "Bearer");
    }
}
