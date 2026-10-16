package com.app.mybackend.aichat.service;

import com.app.mybackend.aichat.dto.request.AiChatRequest;
import com.app.mybackend.aichat.entity.AiConversation;
import com.app.mybackend.aichat.entity.AiMessage;
import org.springframework.stereotype.Service;

import java.util.List;

/**
*
* 클래스명 : AiChatService
* 역할 : AI 대화 요청 준비, 답변 생성 및 메시지 저장을 처리하는 서비스
* 생성일 : 2026-09-17
*
**/
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

    public String generate(PreparedChat chat) {
        return assistantService.respond(chat.message(), chat.history());
    }

    public AiMessage appendAssistant(Long conversationId, String content) {
        return conversationService.append(conversationId, "assistant", content);
    }

    public AiMessage appendException(Long conversationId, String content) {
        return conversationService.append(conversationId, "exception", content);
    }

    public record PreparedChat(
            Long conversationId,
            String message,
            List<AiMessage> history
    ) {
    }
}
