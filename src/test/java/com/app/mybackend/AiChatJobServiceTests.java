package com.app.mybackend;

import com.app.mybackend.aichat.dto.request.AiChatRequest;
import com.app.mybackend.aichat.dto.response.AiChatJobResponse;
import com.app.mybackend.aichat.entity.AiMessage;
import com.app.mybackend.aichat.service.AiChatJobService;
import com.app.mybackend.aichat.service.AiChatService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiChatJobServiceTests {
    private static final String CANCEL_MESSAGE = "AI 대화 생성 중 요청이 중단되었습니다. 잠시 후 다시 시도해주세요.";

    @Test
    void cancellationSavesExceptionAndReleasesConversationForNextRequest() throws Exception {
        AiChatService chatService = mock(AiChatService.class);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            AiChatJobService jobs = new AiChatJobService(chatService, executor);
            when(chatService.prepare(any())).thenReturn(new AiChatService.PreparedChat(7L, "질문", List.of()));
            CountDownLatch started = new CountDownLatch(1);
            when(chatService.generate(any())).thenAnswer(call -> {
                started.countDown();
                new CountDownLatch(1).await();
                return "늦은 답변";
            });
            when(chatService.appendException(eq(7L), eq(CANCEL_MESSAGE)))
                    .thenReturn(message("exception", CANCEL_MESSAGE));

            AiChatJobResponse first = jobs.start(new AiChatRequest(7L, "질문"));
            assertTrue(started.await(2, TimeUnit.SECONDS));
            AiChatJobResponse cancelled = jobs.cancel(first.requestId());
            assertEquals("completed", cancelled.status());
            assertTrue(cancelled.failed());
            assertEquals("exception", cancelled.message().role());
            assertEquals(CANCEL_MESSAGE, cancelled.message().content());
            assertFalse(jobs.isProcessing(7L));

            AiChatJobResponse second = jobs.start(new AiChatRequest(7L, "다음 질문"));
            assertEquals("processing", second.status());
            jobs.cancel(second.requestId());
            verify(chatService, never()).appendAssistant(eq(7L), eq("늦은 답변"));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void generationFailureIsSavedAsExceptionMessage() throws Exception {
        AiChatService chatService = mock(AiChatService.class);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            AiChatJobService jobs = new AiChatJobService(chatService, executor);
            when(chatService.prepare(any())).thenReturn(new AiChatService.PreparedChat(7L, "질문", List.of()));
            when(chatService.generate(any())).thenThrow(new RuntimeException("failure"));
            when(chatService.appendException(eq(7L), any()))
                    .thenAnswer(call -> message("exception", call.getArgument(1)));

            AiChatJobResponse started = jobs.start(new AiChatRequest(7L, "질문"));
            AiChatJobResponse result = jobs.find(started.requestId());
            for (int attempt = 0; attempt < 100 && "processing".equals(result.status()); attempt++) {
                Thread.sleep(10);
                result = jobs.find(started.requestId());
            }
            assertEquals("completed", result.status());
            assertTrue(result.failed());
            assertEquals("exception", result.message().role());
            verify(chatService).appendException(eq(7L), any());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void successfulGenerationSavesOneAssistantMessage() throws Exception {
        AiChatService chatService = mock(AiChatService.class);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            AiChatJobService jobs = new AiChatJobService(chatService, executor);
            when(chatService.prepare(any())).thenReturn(new AiChatService.PreparedChat(7L, "질문", List.of()));
            when(chatService.generate(any())).thenReturn("정상 답변");
            when(chatService.appendAssistant(7L, "정상 답변"))
                    .thenReturn(message("assistant", "정상 답변"));

            AiChatJobResponse started = jobs.start(new AiChatRequest(7L, "질문"));
            AiChatJobResponse result = jobs.find(started.requestId());
            for (int attempt = 0; attempt < 100 && "processing".equals(result.status()); attempt++) {
                Thread.sleep(10);
                result = jobs.find(started.requestId());
            }
            assertEquals("completed", result.status());
            assertFalse(result.failed());
            assertEquals("assistant", result.message().role());
            verify(chatService).appendAssistant(7L, "정상 답변");
            verify(chatService, never()).appendException(any(), any());
        } finally {
            executor.shutdownNow();
        }
    }

    private AiMessage message(String role, String content) {
        AiMessage message = new AiMessage();
        message.setMessageId(1L);
        message.setConversationId(7L);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }
}
