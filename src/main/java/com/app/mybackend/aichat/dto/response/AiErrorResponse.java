package com.app.mybackend.aichat.dto.response;

/**
*
* 클래스명 : AiErrorResponse
* 역할 : AI 채팅 오류 코드와 안내 문구를 전달하는 응답 DTO
* 생성일 : 2026-09-17
*
**/
public record AiErrorResponse(
        String code,
        String message
) {
}
