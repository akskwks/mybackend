package com.app.mybackend.user.dto;

/**
*
* 클래스명 : UserResponse
* 역할 : 인증된 사용자 정보를 클라이언트에 전달하는 응답 DTO
* 생성일 : 2026-10-14
*
**/
public record UserResponse(
        Long userNum,
        String userId,
        String userName,
        String role
) {
}
