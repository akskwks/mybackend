package com.app.mybackend.aichat.dto.response;

import com.app.mybackend.aichat.entity.AiMessage;

import java.time.LocalDateTime;

/**
*
* 클래스명 : AiMessageResponse
* 역할 : 저장된 AI 메시지 정보를 전달하는 응답 DTO
* 생성일 : 2026-09-17
*
**/
public record AiMessageResponse(
        Long messageId,
        Long conversationId,
        String role,
        String content,
        LocalDateTime createdAt
) {
    public static AiMessageResponse from(AiMessage message) {
        return new AiMessageResponse(
                message.getMessageId(),
                message.getConversationId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
