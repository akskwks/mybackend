package com.app.mybackend.aichat.exception;

import com.app.mybackend.aichat.dto.response.AiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.app.mybackend.aichat")
public class AiChatExceptionHandler {

    @ExceptionHandler(AiChatException.class)
    public ResponseEntity<AiErrorResponse> handleAiChatException(AiChatException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(new AiErrorResponse(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().isEmpty()
                ? "요청 내용을 확인해 주세요."
                : exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(new AiErrorResponse("INVALID_REQUEST", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AiErrorResponse> handleNotFound(IllegalArgumentException exception) {
        return ResponseEntity.status(404).body(new AiErrorResponse("NOT_FOUND", exception.getMessage()));
    }
}
