package com.app.mybackend.aichat.dto.request;

import jakarta.validation.constraints.Size;

/**
*
* 클래스명 : AiConversationCreateRequest
* 역할 : AI 대화방 생성 입력값을 전달하는 요청 DTO
* 생성일 : 2026-09-17
*
**/
public record AiConversationCreateRequest(
        @Size(max = 120, message = "대화 제목은 120자 이하여야 합니다.")
        String title
) {
}
