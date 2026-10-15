package com.app.mybackend.aichat.repository;

import com.app.mybackend.aichat.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    List<AiConversation> findByUserNumOrderByUpdatedAtDesc(Long userNum);
    Optional<AiConversation> findByConversationIdAndUserNum(Long conversationId, Long userNum);
}
