package com.app.mybackend.aichat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiConversationUpdateRequest(
        @NotBlank(message = "대화 제목은 필수입니다.")
        @Size(max = 120, message = "대화 제목은 120자 이하여야 합니다.")
        String title
) {
}
