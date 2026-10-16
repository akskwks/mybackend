package com.app.mybackend.aichat.repository;

import com.app.mybackend.aichat.entity.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
*
* 클래스명 : AiMessageRepository
* 역할 : AI 대화 메시지를 조회하고 저장하는 JPA 저장소
* 생성일 : 2026-09-17
*
**/
public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {
    List<AiMessage> findByConversationIdOrderByCreatedAtAscMessageIdAsc(Long conversationId);
    void deleteByConversationId(Long conversationId);
}
