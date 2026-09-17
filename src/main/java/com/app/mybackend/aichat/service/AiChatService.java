package com.app.mybackend.aichat.service;

import com.app.mybackend.aichat.dto.request.AiChatRequest;
import com.app.mybackend.aichat.dto.response.AiChatResponse;
import com.app.mybackend.aichat.dto.response.AiMessageResponse;
import com.app.mybackend.aichat.entity.AiConversation;
import com.app.mybackend.aichat.entity.AiMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiChatService {

    private final AiConversationService conversationService;
    private final AiAssistantService assistantService;

    public AiChatService(
            AiConversationService conversationService,
            AiAssistantService assistantService
    ) {
        this.conversationService = conversationService;
        this.assistantService = assistantService;
    }

    public AiChatResponse chat(AiChatRequest request) {
        AiConversation conversation = request.conversationId() == null
                ? conversationService.create(request.message())
                : conversationService.findEntity(request.conversationId());
        List<AiMessage> history = conversationService.findRecentMessages(conversation.getConversationId(), 16);

        conversationService.append(conversation.getConversationId(), "user", request.message());
        conversationService.applyFirstMessageTitle(conversation, request.message());
        String answer = assistantService.respond(request.message(), history);
        AiMessage savedAnswer = conversationService.append(conversation.getConversationId(), "assistant", answer);

        return new AiChatResponse(
                conversation.getConversationId(),
                answer,
                AiMessageResponse.from(savedAnswer)
        );
    }
}
