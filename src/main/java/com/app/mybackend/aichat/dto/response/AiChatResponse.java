package com.app.mybackend.aichat.dto.response;

public record AiChatResponse(
        Long conversationId,
        String answer,
        AiMessageResponse message
) {
}
