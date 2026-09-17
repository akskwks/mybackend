package com.app.mybackend.aichat.dto.response;

import com.app.mybackend.aichat.entity.AiMessage;

import java.time.LocalDateTime;

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
