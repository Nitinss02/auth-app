package com.auth.auth_app.dtos;

public record LoginRequest(
        String email,
        String password
) {
}
