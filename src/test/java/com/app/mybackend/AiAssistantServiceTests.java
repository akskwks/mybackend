package com.app.mybackend;

import com.app.mybackend.aichat.service.AiAssistantService;
import com.app.mybackend.aichat.service.AiDateExpressionService;
import com.app.mybackend.aichat.service.AiIntentService;
import com.app.mybackend.aichat.service.AiPromptService;
import com.app.mybackend.aichat.service.OllamaChatGateway;
import com.app.mybackend.aichat.service.ProjectWorkAiAssistant;
import com.app.mybackend.calendar.entity.CalendarEvent;
import com.app.mybackend.calendar.service.CalendarEventService;
import com.app.mybackend.memo.entity.Memo;
import com.app.mybackend.memo.service.MemoService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

class AiAssistantServiceTests {
    private final CalendarEventService calendar = mock(CalendarEventService.class);
    private final MemoService memos = mock(MemoService.class);
    private final OllamaChatGateway ollama = mock(OllamaChatGateway.class);
    private final AiAssistantService assistant = new AiAssistantService(
            new AiIntentService(),
            mock(AiPromptService.class),
            ollama,
            calendar,
            memos,
            mock(ProjectWorkAiAssistant.class),
            new AiDateExpressionService()
    );

    @Test
    void emptyCalendarAnswerKeepsRequestedDatePhrase() {
        when(calendar.findBetween(any(), any())).thenReturn(List.of());

        assertEquals("내일 예정된 일정이 없습니다.", assistant.respond("내일 일정", List.of()));
        assertEquals("모레 예정된 일정이 없습니다.", assistant.respond("모레 일정", List.of()));
        assertEquals("3일 뒤 예정된 일정이 없습니다.", assistant.respond("3일 뒤 일정", List.of()));
    }

    @Test
    void calendarAnswerUsesSavedEventsWithoutMixingHistoryOrModelOutput() {
        CalendarEvent event = new CalendarEvent();
        event.setEventTitle("개발 회의 테스트");
        event.setEventDate(LocalDate.now());
        event.setStartTime(java.time.LocalTime.of(13, 0));
        event.setEndTime(java.time.LocalTime.of(15, 1));
        when(calendar.findBetween(any(), any())).thenReturn(List.of(event));

        assertEquals("오늘 일정은 13:00-15:01 개발 회의 테스트가 예정되어 있습니다.",
                assistant.respond("오늘 일정 알려줘", List.of()));
        verifyNoInteractions(ollama);
    }

    @Test
    void memoLookupUsesCreatedDateForShortQuestions() {
        Memo yesterday = new Memo();
        yesterday.setMemoTitle("어제 기록");
        yesterday.setMemoSort("general");
        yesterday.setMemoCnnt("<p>완료한 작업</p>");
        yesterday.setCreatedAt(LocalDate.now().minusDays(1).atTime(10, 0));

        Memo today = new Memo();
        today.setMemoTitle("오늘 기록");
        today.setMemoSort("general");
        today.setMemoCnnt("<p>새 작업</p>");
        today.setCreatedAt(LocalDate.now().atTime(10, 0));

        when(memos.findAll("")).thenReturn(List.of(yesterday, today));

        String answer = assistant.respond("어제 메모", List.of());
        assertTrue(answer.contains("어제 기록"));
        assertFalse(answer.contains("오늘 기록"));
        assertEquals("그저께 작성된 메모가 없습니다.", assistant.respond("그저께 메모", List.of()));
    }

    @Test
    void calendarDeletionListsTomorrowEventsInTimeOrderAndDeletesSelectedId() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        CalendarEvent afternoon = calendarEvent(11L, tomorrow, 14, "프로젝트 회의");
        CalendarEvent morning = calendarEvent(12L, tomorrow, 9, "자료 정리");
        when(calendar.findBetween(tomorrow, tomorrow)).thenReturn(List.of(afternoon, morning));
        when(calendar.findById(12L)).thenReturn(morning);

        String prompt = assistant.respond("내일 일정 삭제", List.of());
        assertTrue(prompt.indexOf("1. [09:00") < prompt.indexOf("2. [14:00"));
        assertTrue(prompt.contains("프로젝트 회의"));
        var history = List.of(userMessage("내일 일정 삭제"), assistantMessage(prompt));
        assertEquals("목록에 있는 번호를 선택해 주세요.\n\n" + prompt,
                assistant.respond("3", history));
        verify(calendar, never()).delete(any());

        var retryHistory = List.of(userMessage("내일 일정 삭제"), assistantMessage(prompt),
                userMessage("3"), assistantMessage("목록에 있는 번호를 선택해 주세요.\n\n" + prompt));

        assertEquals("'자료 정리' 일정을 삭제했습니다.",
                assistant.respond("1번", retryHistory));
        verify(calendar).delete(12L);
        verify(calendar, never()).delete(11L);
        verifyNoInteractions(ollama);
    }

    @Test
    void calendarDeletionWithNoEventsReturnsEmptyMessage() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        when(calendar.findBetween(tomorrow, tomorrow)).thenReturn(List.of());

        assertEquals("등록된 일정이 없습니다.", assistant.respond("내일 일정 삭제", List.of()));
        verify(calendar, never()).delete(any());
    }

    private CalendarEvent calendarEvent(Long id, LocalDate date, int hour, String title) {
        CalendarEvent event = new CalendarEvent();
        event.setEventId(id);
        event.setEventDate(date);
        event.setStartTime(java.time.LocalTime.of(hour, 0));
        event.setEndTime(java.time.LocalTime.of(hour + 1, 0));
        event.setEventTitle(title);
        return event;
    }

    private com.app.mybackend.aichat.entity.AiMessage assistantMessage(String content) {
        var message = new com.app.mybackend.aichat.entity.AiMessage();
        message.setRole("assistant");
        message.setContent(content);
        return message;
    }

    private com.app.mybackend.aichat.entity.AiMessage userMessage(String content) {
        var message = new com.app.mybackend.aichat.entity.AiMessage();
        message.setRole("user");
        message.setContent(content);
        return message;
    }
}
