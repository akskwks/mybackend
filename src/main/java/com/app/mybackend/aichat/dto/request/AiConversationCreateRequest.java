package com.app.mybackend.aichat.dto.request;

import jakarta.validation.constraints.Size;

public record AiConversationCreateRequest(
        @Size(max = 120, message = "대화 제목은 120자 이하여야 합니다.")
        String title
) {
}
