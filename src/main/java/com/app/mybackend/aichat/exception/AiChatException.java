package com.app.mybackend.aichat.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

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
