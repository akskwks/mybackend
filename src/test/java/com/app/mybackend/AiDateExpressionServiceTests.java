package com.app.mybackend;

import com.app.mybackend.aichat.service.AiDateExpressionService;
import com.app.mybackend.aichat.service.AiDateExpressionService.DateSelection;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AiDateExpressionServiceTests {
    private final AiDateExpressionService service = new AiDateExpressionService();
    private final LocalDate today = LocalDate.of(2026, 10, 7);

    @Test
    void resolvesRelativeDaysWeeksAndMonths() {
        assertDay("오늘 일정", today);
        assertDay("내일 일정", today.plusDays(1));
        assertDay("모레 일정", today.plusDays(2));
        assertDay("3일 뒤 일정", today.plusDays(3));
        assertDay("4일 후 일정", today.plusDays(4));
        assertDay("7일 뒤 일정", today.plusDays(7));
        assertDay("일주일 뒤 일정", today.plusWeeks(1));
        assertDay("2주 후 일정", today.plusWeeks(2));
        assertDay("한 달 뒤 일정", today.plusMonths(1));
        assertDay("어제 메모", today.minusDays(1));
        assertDay("그저께 메모", today.minusDays(2));
        assertDay("3일 전에 작성한 메모", today.minusDays(3));
        assertDay("일주일 전에 작성한 메모", today.minusWeeks(1));
        assertDay("한 달 전에 작성한 메모", today.minusMonths(1));
    }

    @Test
    void resolvesNamedPeriodsAndExactDates() {
        assertRange("지난주 메모", LocalDate.of(2026, 9, 28), LocalDate.of(2026, 10, 4));
        assertRange("이번 주 프로젝트", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 11));
        assertRange("이번 주말 일정", LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 11));
        assertRange("다음 달 프로젝트", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        assertDay("2026-10-09 일정", LocalDate.of(2026, 10, 9));
        assertDay("10월 9일 일정", LocalDate.of(2026, 10, 9));
        assertNull(service.resolve("Docker 관련 메모", today));
    }

    @Test
    void keepsOriginalExpressionForResponseAndKeywordExtraction() {
        DateSelection date = service.resolve("3일 뒤 일정", today);
        assertEquals("3일 뒤", date.expression());
        assertEquals(" 작성한 메모", service.withoutDateExpression("3일 전에 작성한 메모"));
    }

    private void assertDay(String message, LocalDate expected) {
        assertRange(message, expected, expected);
    }

    private void assertRange(String message, LocalDate start, LocalDate end) {
        DateSelection date = service.resolve(message, today);
        assertEquals(start, date.start(), message);
        assertEquals(end, date.end(), message);
    }
}
