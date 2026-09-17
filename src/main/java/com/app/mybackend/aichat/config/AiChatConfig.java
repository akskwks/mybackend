package com.app.mybackend.aichat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AiChatConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService aiChatExecutor() {
        return Executors.newFixedThreadPool(2);
    }
}
