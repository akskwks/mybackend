package com.app.mybackend.aichat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
*
* 클래스명 : AiChatConfig
* 역할 : AI 채팅과 요청 처리용 실행기를 설정하는 구성 클래스
* 생성일 : 2026-09-17
*
**/
@Configuration
public class AiChatConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService aiChatExecutor() {
        return new DelegatingSecurityContextExecutorService(Executors.newFixedThreadPool(2));
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService aiRequestExecutor() {
        return new DelegatingSecurityContextExecutorService(Executors.newFixedThreadPool(4));
    }
}
