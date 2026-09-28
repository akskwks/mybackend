package com.app.mybackend.aichat.dto.response;

public record AiChatJobResponse(
        String requestId,
        Long conversationId,
        String status,
        boolean failed,
        AiMessageResponse message
) {
}
