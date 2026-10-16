package com.app.mybackend.aichat.dto.response;

/**
*
* 클래스명 : AiChatJobResponse
* 역할 : 비동기 AI 답변 작업 상태를 전달하는 응답 DTO
* 생성일 : 2026-09-17
*
**/
public record AiChatJobResponse(
        String requestId,
        Long conversationId,
        String status,
        boolean failed,
        AiMessageResponse message
) {
}
