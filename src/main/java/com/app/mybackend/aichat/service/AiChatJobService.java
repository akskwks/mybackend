package com.app.mybackend.aichat.service;

import com.app.mybackend.aichat.dto.request.AiChatRequest;
import com.app.mybackend.aichat.dto.response.AiChatJobResponse;
import com.app.mybackend.aichat.dto.response.AiMessageResponse;
import com.app.mybackend.aichat.entity.AiMessage;
import com.app.mybackend.aichat.exception.AiChatException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Service
public class AiChatJobService {

    private static final Duration COMPLETED_JOB_TTL = Duration.ofHours(2);

    private final AiChatService chatService;
    private final ExecutorService requestExecutor;
    private final Map<UUID, JobState> jobs = new ConcurrentHashMap<>();
    private final Map<Long, UUID> activeConversations = new ConcurrentHashMap<>();

    public AiChatJobService(
            AiChatService chatService,
            @Qualifier("aiRequestExecutor") ExecutorService requestExecutor
    ) {
        this.chatService = chatService;
        this.requestExecutor = requestExecutor;
    }

    public synchronized AiChatJobResponse start(AiChatRequest request) {
        cleanupCompletedJobs();
        if (request.conversationId() != null && activeConversations.containsKey(request.conversationId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이 대화에서 이미 답변을 생성하고 있습니다."
            );
        }

        AiChatService.PreparedChat prepared = chatService.prepare(request);
        UUID requestId = UUID.randomUUID();
        UUID existing = activeConversations.putIfAbsent(prepared.conversationId(), requestId);
        if (existing != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이 대화에서 이미 답변을 생성하고 있습니다."
            );
        }

        JobState job = new JobState(requestId, prepared.conversationId());
        jobs.put(requestId, job);
        try {
            requestExecutor.submit(() -> process(job, prepared));
        } catch (RuntimeException exception) {
            try {
                AiMessage savedError = chatService.appendAssistant(
                        prepared.conversationId(),
                        "AI 요청을 시작하지 못했습니다. 잠시 후 다시 시도해 주세요."
                );
                job.complete(savedError, true);
            } catch (RuntimeException saveException) {
                job.failWithoutMessage();
            } finally {
                activeConversations.remove(prepared.conversationId(), requestId);
            }
        }
        return job.response();
    }

    public AiChatJobResponse find(String requestId) {
        UUID id;
        try {
            id = UUID.fromString(requestId);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AI 요청 작업을 찾을 수 없습니다.");
        }
        JobState job = jobs.get(id);
        if (job == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AI 요청 작업을 찾을 수 없습니다.");
        }
        return job.response();
    }

    public boolean isProcessing(Long conversationId) {
        return activeConversations.containsKey(conversationId);
    }

    private void process(JobState job, AiChatService.PreparedChat prepared) {
        try {
            AiMessage message = chatService.complete(prepared);
            job.complete(message, false);
        } catch (Throwable exception) {
            String errorMessage = userMessage(exception);
            try {
                AiMessage savedError = chatService.appendAssistant(prepared.conversationId(), errorMessage);
                job.complete(savedError, true);
            } catch (Throwable saveException) {
                job.failWithoutMessage();
            }
        } finally {
            activeConversations.remove(prepared.conversationId(), job.requestId());
        }
    }

    private String userMessage(Throwable exception) {
        if (exception instanceof AiChatException aiException) return aiException.getMessage();
        if (exception instanceof ResponseStatusException responseException
                && responseException.getReason() != null) {
            return responseException.getReason();
        }
        if (exception instanceof IllegalArgumentException && exception.getMessage() != null) {
            return exception.getMessage();
        }
        return "AI 요청을 처리하는 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
    }

    private void cleanupCompletedJobs() {
        Instant threshold = Instant.now().minus(COMPLETED_JOB_TTL);
        jobs.entrySet().removeIf(entry -> entry.getValue().completedBefore(threshold));
    }

    private static final class JobState {
        private final UUID requestId;
        private final Long conversationId;
        private volatile String status = "processing";
        private volatile boolean failed;
        private volatile AiMessageResponse message;
        private volatile Instant completedAt;

        private JobState(UUID requestId, Long conversationId) {
            this.requestId = requestId;
            this.conversationId = conversationId;
        }

        private UUID requestId() {
            return requestId;
        }

        private void complete(AiMessage savedMessage, boolean failed) {
            this.message = AiMessageResponse.from(savedMessage);
            this.failed = failed;
            this.completedAt = Instant.now();
            this.status = "completed";
        }

        private void failWithoutMessage() {
            this.failed = true;
            this.completedAt = Instant.now();
            this.status = "completed";
        }

        private boolean completedBefore(Instant threshold) {
            return completedAt != null && completedAt.isBefore(threshold);
        }

        private AiChatJobResponse response() {
            return new AiChatJobResponse(
                    requestId.toString(), conversationId, status, failed, message
            );
        }
    }
}
