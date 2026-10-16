package com.app.mybackend.aichat.dto.response;

import com.app.mybackend.aichat.entity.AiConversation;

import java.time.LocalDateTime;

/**
*
* 클래스명 : AiConversationResponse
* 역할 : AI 대화방 정보를 클라이언트에 전달하는 응답 DTO
* 생성일 : 2026-09-17
*
**/
public record AiConversationResponse(
        Long conversationId,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AiConversationResponse from(AiConversation conversation) {
        return new AiConversationResponse(
                conversation.getConversationId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }
}
