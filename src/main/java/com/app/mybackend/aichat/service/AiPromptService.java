package com.app.mybackend.aichat.service;

import com.app.mybackend.aichat.entity.AiMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AiPromptService {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String systemPrompt() {
        return """
                당신은 개인 생산성 서비스 MyApp의 AI Assistant입니다.
                사용자의 일정, 메모, 프로젝트와 업무를 관리하는 일을 돕고, 일반 질문에도 정확하고 간결하게 답변합니다.
                제공된 MyApp 데이터만 사실로 사용하고, 없는 일정, 메모, 프로젝트 또는 업무를 만들어내지 마세요.
                답변은 한국어를 기본으로 하며 Markdown을 사용할 수 있습니다.
                코드가 포함되면 fenced code block과 적절한 언어명을 사용하세요.
                현재 시각: %s
                """.formatted(LocalDateTime.now().format(DATE_TIME_FORMAT));
    }

    public String userPrompt(String userInput, List<AiMessage> history, String myAppData) {
        return """
                [이전 대화 Context]
                %s

                [MyApp 데이터]
                %s

                [사용자 입력]
                %s

                [응답 지침]
                사용자 입력에 직접 답변하세요. MyApp 데이터가 제공되었다면 그 범위 안에서만 설명하세요.
                """.formatted(formatHistory(history), emptyData(myAppData), userInput);
    }

    private String formatHistory(List<AiMessage> history) {
        if (history.isEmpty()) return "이전 대화 없음";
        return history.stream()
                .map(message -> ("user".equals(message.getRole()) ? "사용자" : "AI") + ": " + message.getContent())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("이전 대화 없음");
    }

    private String emptyData(String data) {
        return data == null || data.isBlank() ? "전달된 데이터 없음" : data;
    }
}
