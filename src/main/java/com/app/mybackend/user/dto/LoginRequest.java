package com.app.mybackend.user.dto;

/**
*
* 클래스명 : LoginRequest
* 역할 : 로그인 아이디와 비밀번호를 전달하는 요청 DTO
* 생성일 : 2026-10-14
*
**/
public record LoginRequest(
        String userId,
        String password
) {
}
