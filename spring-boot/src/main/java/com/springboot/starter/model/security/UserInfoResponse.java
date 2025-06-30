package com.springboot.starter.model.security;

public record UserInfoResponse(
    String username,
    String email
) {
}
