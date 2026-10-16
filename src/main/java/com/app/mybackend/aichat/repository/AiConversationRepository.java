package com.app.mybackend.aichat.repository;

import com.app.mybackend.aichat.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
*
* 클래스명 : AiConversationRepository
* 역할 : 사용자별 AI 대화방을 조회하고 저장하는 JPA 저장소
* 생성일 : 2026-09-17
*
**/
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    List<AiConversation> findByUserNumOrderByUpdatedAtDesc(Long userNum);
    Optional<AiConversation> findByConversationIdAndUserNum(Long conversationId, Long userNum);
}
