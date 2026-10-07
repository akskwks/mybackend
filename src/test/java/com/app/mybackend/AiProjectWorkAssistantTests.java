package com.app.mybackend;

import com.app.mybackend.aichat.service.AiDateExpressionService;
import com.app.mybackend.aichat.service.ProjectWorkAiAssistant;
import com.app.mybackend.aichat.service.ProjectWorkAiTool;
import com.app.mybackend.work.entity.ProjectList;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static com.app.mybackend.aichat.service.AiIntentService.Intent.PROJECT_LIST_QUERY;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiProjectWorkAssistantTests {
    @Test
    void filtersProjectListToRequestedWeek() {
        LocalDate today = LocalDate.now();
        ProjectList active = new ProjectList();
        active.setProjectId(1L);
        active.setProjectName("진행 중 프로젝트");
        active.setStartDate(today.with(DayOfWeek.MONDAY));
        active.setEndDate(today.with(DayOfWeek.SUNDAY));

        ProjectList old = new ProjectList();
        old.setProjectId(2L);
        old.setProjectName("지난 프로젝트");
        old.setStartDate(today.minusMonths(2));
        old.setEndDate(today.minusMonths(1));

        ProjectWorkAiTool tool = mock(ProjectWorkAiTool.class);
        when(tool.findProjects()).thenReturn(List.of(active, old));
        ProjectWorkAiAssistant assistant = new ProjectWorkAiAssistant(tool, new AiDateExpressionService());

        String answer = assistant.respond(PROJECT_LIST_QUERY, "이번 주 프로젝트", List.of());
        assertTrue(answer.contains("진행 중 프로젝트"));
        assertFalse(answer.contains("지난 프로젝트"));
    }
}
