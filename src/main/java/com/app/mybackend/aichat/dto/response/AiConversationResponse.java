package com.app.mybackend.aichat.dto.response;

import com.app.mybackend.aichat.entity.AiConversation;

import java.time.LocalDateTime;

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
