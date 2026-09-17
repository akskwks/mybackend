package com.app.mybackend.aichat.service;

import com.app.mybackend.aichat.dto.request.AiChatRequest;
import com.app.mybackend.aichat.dto.response.AiChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiChatService {

    private final ChatClient chatClient;

    public AiChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public AiChatResponse chat(AiChatRequest request) {
        String answer = chatClient
                .prompt()
                .user(request.message())
                .call()
                .content();

        return new AiChatResponse(
                answer == null ? "" : answer
        );
    }
}
