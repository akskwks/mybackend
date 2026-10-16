package com.app.mybackend.aichat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
*
* 클래스명 : AiChatRequest
* 역할 : AI 대화방 식별자와 사용자 질문을 전달하는 요청 DTO
* 생성일 : 2026-09-17
*
**/
public record AiChatRequest(
        Long conversationId,

        @NotBlank(message = "질문은 필수입니다.")
        @Size(max = 4000, message = "질문은 4000자 이하여야 합니다.")
        String message

) {
}
