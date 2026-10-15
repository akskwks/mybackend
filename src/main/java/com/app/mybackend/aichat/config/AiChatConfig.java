package com.app.mybackend.aichat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
