package com.app.mybackend.aichat.repository;

import com.app.mybackend.aichat.entity.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {
    List<AiMessage> findByConversationIdOrderByCreatedAtAscMessageIdAsc(Long conversationId);
    void deleteByConversationId(Long conversationId);
}
