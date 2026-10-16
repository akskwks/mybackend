package com.app.mybackend.aichat.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
*
* 클래스명 : AiChatException
* 역할 : AI 채팅 오류 코드와 HTTP 상태를 담는 예외
* 생성일 : 2026-09-17
*
**/
@Getter
public class AiChatException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public AiChatException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

////////// @Getter 사용 //////////
//    public String getCode() {
//        return code;
//    }
//
//    public HttpStatus getStatus() {
//        return status;
//    }
}
