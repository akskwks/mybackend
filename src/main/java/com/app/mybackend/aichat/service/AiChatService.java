package com.app.mybackend.aichat.service;

import com.app.mybackend.aichat.dto.request.AiChatRequest;
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

    public PreparedChat prepare(AiChatRequest request) {
        AiConversation conversation = request.conversationId() == null
                ? conversationService.create(request.message())
                : conversationService.findEntity(request.conversationId());
        List<AiMessage> history = conversationService.findRecentMessages(conversation.getConversationId(), 16);

        conversationService.append(conversation.getConversationId(), "user", request.message());
        conversationService.applyFirstMessageTitle(conversation, request.message());
        return new PreparedChat(conversation.getConversationId(), request.message(), List.copyOf(history));
    }

    public AiMessage complete(PreparedChat chat) {
        String answer = assistantService.respond(chat.message(), chat.history());
        return appendAssistant(chat.conversationId(), answer);
    }

    public AiMessage appendAssistant(Long conversationId, String content) {
        return conversationService.append(conversationId, "assistant", content);
    }

    public record PreparedChat(
            Long conversationId,
            String message,
            List<AiMessage> history
    ) {
    }
}
