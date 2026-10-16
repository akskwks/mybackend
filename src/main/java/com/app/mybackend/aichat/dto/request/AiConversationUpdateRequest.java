package com.app.mybackend.aichat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
*
* 클래스명 : AiConversationUpdateRequest
* 역할 : AI 대화방명 수정 입력값을 전달하는 요청 DTO
* 생성일 : 2026-09-17
*
**/
public record AiConversationUpdateRequest(
        @NotBlank(message = "대화 제목은 필수입니다.")
        @Size(max = 120, message = "대화 제목은 120자 이하여야 합니다.")
        String title
) {
}
