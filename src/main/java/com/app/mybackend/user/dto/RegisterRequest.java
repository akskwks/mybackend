package com.app.mybackend.user.dto;

/**
*
* 클래스명 : RegisterRequest
* 역할 : 회원가입 입력값을 전달하는 요청 DTO
* 생성일 : 2026-10-14
*
**/
public record RegisterRequest(
        String userId,
        String password,
        String passwordConfirm,
        String userName
) {
}
