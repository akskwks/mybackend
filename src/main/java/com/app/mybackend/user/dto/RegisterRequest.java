package com.app.mybackend.user.dto;

public record RegisterRequest(
        String userId,
        String password,
        String passwordConfirm,
        String userName
) {
}
