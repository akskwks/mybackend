package com.app.mybackend.aichat.service;

import com.app.mybackend.aichat.exception.AiChatException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class OllamaChatGateway {
    private final ChatClient chatClient;
    private final ExecutorService aiChatExecutor;
    private final long timeoutSeconds;

    public OllamaChatGateway(
            ChatClient.Builder chatClientBuilder,
            @Qualifier("aiChatExecutor")
            ExecutorService aiChatExecutor,
            @Value("${myapp.ai.timeout-seconds:90}") long timeoutSeconds
    ) {
        this.chatClient = chatClientBuilder.build();
        this.aiChatExecutor = aiChatExecutor;
        this.timeoutSeconds = timeoutSeconds;
    }

    public String generate(String systemPrompt, String userPrompt) {
        Future<String> request = aiChatExecutor.submit(() -> chatClient
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content());

        try {
            String answer = request.get(timeoutSeconds, TimeUnit.SECONDS);
            if (answer == null || answer.isBlank()) {
                throw new AiChatException(
                        "EMPTY_AI_RESPONSE",
                        "AI가 빈 응답을 반환했습니다. 잠시 후 다시 시도해 주세요.",
                        HttpStatus.BAD_GATEWAY
                );
            }
            return answer;
        } catch (TimeoutException exception) {
            request.cancel(true);
            throw new AiChatException(
                    "AI_TIMEOUT",
                    "AI 답변 생성 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요.",
                    HttpStatus.GATEWAY_TIMEOUT
            );
        } catch (InterruptedException exception) {
            request.cancel(true);
            Thread.currentThread().interrupt();
            throw new AiChatException(
                    "AI_REQUEST_INTERRUPTED",
                    "AI 요청이 중단되었습니다. 잠시 후 다시 시도해 주세요.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        } catch (ExecutionException exception) {
            throw mapException(exception.getCause());
        }
    }

    private AiChatException mapException(Throwable throwable) {
        if (throwable instanceof AiChatException aiChatException) {
            return aiChatException;
        }
        String detail = collectMessages(throwable).toLowerCase(Locale.ROOT);
        if (detail.contains("model") && (detail.contains("not found") || detail.contains("does not exist"))) {
            return new AiChatException(
                    "OLLAMA_MODEL_NOT_FOUND",
                    "설정된 Qwen 모델을 Ollama에서 찾을 수 없습니다. 모델 설치 상태를 확인해 주세요.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
        if (detail.contains("connection refused") || detail.contains("connect") || detail.contains("ollama")) {
            return new AiChatException(
                    "OLLAMA_UNAVAILABLE",
                    "Ollama 서버에 연결할 수 없습니다. Ollama 실행 상태를 확인해 주세요.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
        return new AiChatException(
                "AI_RESPONSE_FAILED",
                "AI 답변을 생성하는 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.",
                HttpStatus.BAD_GATEWAY
        );
    }

    private String collectMessages(Throwable throwable) {
        StringBuilder messages = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null) messages.append(' ').append(current.getMessage());
            current = current.getCause();
        }
        return messages.toString();
    }
}
