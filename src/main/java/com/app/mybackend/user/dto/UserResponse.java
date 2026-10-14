package com.app.mybackend.user.dto;

public record UserResponse(Long userNum, String userId,
                           String userName, String role) {
}
