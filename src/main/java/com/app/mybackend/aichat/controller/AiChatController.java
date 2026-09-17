package com.app.mybackend.aichat.controller;

import com.app.mybackend.aichat.dto.request.AiConversationCreateRequest;
import com.app.mybackend.aichat.dto.request.AiConversationUpdateRequest;
import com.app.mybackend.aichat.dto.request.AiChatRequest;
import com.app.mybackend.aichat.dto.response.AiChatResponse;
import com.app.mybackend.aichat.dto.response.AiConversationResponse;
import com.app.mybackend.aichat.dto.response.AiMessageResponse;
import com.app.mybackend.aichat.entity.AiConversation;
import com.app.mybackend.aichat.service.AiChatService;
import com.app.mybackend.aichat.service.AiConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;
    private final AiConversationService conversationService;

    @GetMapping("/conversations")
    public List<AiConversationResponse> conversations() {
        return conversationService.findAll();
    }

    @PostMapping("/conversations")
    public AiConversationResponse createConversation(
            @Valid @RequestBody(required = false) AiConversationCreateRequest request
    ) {
        AiConversation conversation = conversationService.create(request == null ? null : request.title());
        return AiConversationResponse.from(conversation);
    }

    @PutMapping("/conversations/{conversationId}")
    public AiConversationResponse renameConversation(
            @PathVariable Long conversationId,
            @Valid @RequestBody AiConversationUpdateRequest request
    ) {
        return AiConversationResponse.from(conversationService.rename(conversationId, request.title()));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<AiMessageResponse> messages(@PathVariable Long conversationId) {
        return conversationService.findMessages(conversationId);
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long conversationId) {
        conversationService.delete(conversationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @Valid @RequestBody AiChatRequest request
    ) {
        return ResponseEntity.ok(aiChatService.chat(request));
    }
}
