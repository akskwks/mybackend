package com.app.mybackend.aichat.service;

import com.app.mybackend.aichat.dto.response.AiConversationResponse;
import com.app.mybackend.aichat.dto.response.AiMessageResponse;
import com.app.mybackend.aichat.entity.AiConversation;
import com.app.mybackend.aichat.entity.AiMessage;
import com.app.mybackend.aichat.repository.AiConversationRepository;
import com.app.mybackend.aichat.repository.AiMessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AiConversationService {
    private static final String DEFAULT_TITLE = "새 대화";

    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;

    public AiConversationService(
            AiConversationRepository conversationRepository,
            AiMessageRepository messageRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    public List<AiConversationResponse> findAll() {
        return conversationRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(AiConversationResponse::from)
                .toList();
    }

    @Transactional
    public AiConversation create(String title) {
        AiConversation conversation = new AiConversation();
        conversation.setTitle(normalizeTitle(title));
        return conversationRepository.save(conversation);
    }

    @Transactional
    public AiConversation rename(Long conversationId, String title) {
        AiConversation conversation = findEntity(conversationId);
        conversation.setTitle(title.trim());
        return conversationRepository.save(conversation);
    }

    public AiConversation findEntity(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("대화를 찾을 수 없습니다."));
    }

    public List<AiMessageResponse> findMessages(Long conversationId) {
        findEntity(conversationId);
        return messageRepository.findByConversationIdOrderByCreatedAtAscMessageIdAsc(conversationId).stream()
                .map(AiMessageResponse::from)
                .toList();
    }

    public List<AiMessage> findRecentMessages(Long conversationId, int limit) {
        List<AiMessage> messages = messageRepository
                .findByConversationIdOrderByCreatedAtAscMessageIdAsc(conversationId);
        return messages.subList(Math.max(0, messages.size() - limit), messages.size());
    }

    @Transactional
    public AiMessage append(Long conversationId, String role, String content) {
        AiConversation conversation = findEntity(conversationId);
        AiMessage message = new AiMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        AiMessage saved = messageRepository.save(message);

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        return saved;
    }

    @Transactional
    public void applyFirstMessageTitle(AiConversation conversation, String message) {
        if (!DEFAULT_TITLE.equals(conversation.getTitle())) return;
        conversation.setTitle(toTitle(message));
        conversationRepository.save(conversation);
    }

    @Transactional
    public void delete(Long conversationId) {
        findEntity(conversationId);
        messageRepository.deleteByConversationId(conversationId);
        conversationRepository.deleteById(conversationId);
    }

    private String normalizeTitle(String title) {
        return title == null || title.isBlank() ? DEFAULT_TITLE : title.trim();
    }

    private String toTitle(String message) {
        String normalized = message.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 36 ? normalized : normalized.substring(0, 36) + "...";
    }
}
