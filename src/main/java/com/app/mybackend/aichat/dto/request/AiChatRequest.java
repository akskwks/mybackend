package com.app.mybackend.aichat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatRequest(

        Long conversationId,

        @NotBlank(message = "질문은 필수입니다.")
        @Size(max = 4000, message = "질문은 4000자 이하여야 합니다.")
        String message

) {
}
