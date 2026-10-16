package com.app.mybackend.user.dto;

public record LoginRequest(
        String userId,
        String password
) {
}
