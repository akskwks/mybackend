package com.app.mybackend;

import com.app.mybackend.aichat.service.AiDateExpressionService;
import com.app.mybackend.aichat.entity.AiMessage;
import com.app.mybackend.aichat.service.ProjectWorkAiAssistant;
import com.app.mybackend.aichat.service.ProjectWorkAiTool;
import com.app.mybackend.work.dto.ProjectListRequest;
import com.app.mybackend.work.entity.ProjectList;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static com.app.mybackend.aichat.service.AiIntentService.Intent.PROJECT_LIST_QUERY;
import static com.app.mybackend.aichat.service.AiIntentService.Intent.PROJECT_CREATE;
import static com.app.mybackend.aichat.service.AiIntentService.Intent.PROJECT_UPDATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiProjectWorkAssistantTests {
    private final ProjectWorkAiTool tool = mock(ProjectWorkAiTool.class);
    private final ProjectWorkAiAssistant assistant = new ProjectWorkAiAssistant(tool, new AiDateExpressionService());

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

    @Test
    void createsProjectFromKeywordsWithPlannedStatusByDefault() {
        when(tool.createProject(any())).thenAnswer(call -> fromRequest(1L, call.getArgument(0)));

        String answer = assistant.respond(PROJECT_CREATE,
                "프로젝트 추가, 내근, 테스트, 2026-10-10, 2026-12-31", List.of());
        ArgumentCaptor<ProjectListRequest> request = ArgumentCaptor.forClass(ProjectListRequest.class);
        verify(tool).createProject(request.capture());
        assertEquals("테스트", request.getValue().projectName());
        assertEquals("office", request.getValue().workEnvironment());
        assertEquals("planned", request.getValue().projectStatus());
        assertEquals(LocalDate.of(2026, 10, 10), request.getValue().startDate());
        assertTrue(answer.contains("예정"));
    }

    @Test
    void createsProjectFromNaturalPhraseWithMonthDayDates() {
        when(tool.createProject(any())).thenAnswer(call -> fromRequest(1L, call.getArgument(0)));

        assistant.respond(PROJECT_CREATE, "내근 테스트 프로젝트 10월 1일부터 12월 31일까지 추가", List.of());
        ArgumentCaptor<ProjectListRequest> request = ArgumentCaptor.forClass(ProjectListRequest.class);
        verify(tool).createProject(request.capture());
        assertEquals("테스트", request.getValue().projectName());
        assertEquals("planned", request.getValue().projectStatus());
        assertEquals(LocalDate.of(LocalDate.now().getYear(), 10, 1), request.getValue().startDate());
        assertEquals(LocalDate.of(LocalDate.now().getYear(), 12, 31), request.getValue().endDate());
    }

    @Test
    void updatesOnlyRequestedFieldForUniquePartialName() {
        ProjectList project = project(1L, "코리안리 특종보험 AI Assistant 프로젝트");
        when(tool.findProjects()).thenReturn(List.of(project));
        when(tool.updateProject(eq(1L), any())).thenAnswer(call -> fromRequest(1L, call.getArgument(1)));

        String answer = assistant.respond(PROJECT_UPDATE, "코리안리 상태 진행중", List.of());
        ArgumentCaptor<ProjectListRequest> request = ArgumentCaptor.forClass(ProjectListRequest.class);
        verify(tool).updateProject(eq(1L), request.capture());
        assertEquals("in_progress", request.getValue().projectStatus());
        assertEquals("office", request.getValue().workEnvironment());
        assertEquals(project.getStartDate(), request.getValue().startDate());
        assertTrue(answer.contains("진행중"));
    }

    @Test
    void requiresChoiceForAmbiguousKeywordAndContinuesOriginalUpdate() {
        ProjectList first = project(1L, "코리안리 특종보험 AI Assistant 프로젝트");
        ProjectList second = project(2L, "코리안리 계약관리 시스템 프로젝트");
        when(tool.findProjects()).thenReturn(List.of(first, second));
        when(tool.updateProject(eq(1L), any())).thenAnswer(call -> fromRequest(1L, call.getArgument(1)));
        String request = "코리안리 시작일 11/1";

        String choices = assistant.respond(PROJECT_UPDATE, request, List.of());
        assertTrue(choices.contains("1. " + first.getProjectName()));
        assertTrue(choices.contains("2. " + second.getProjectName()));
        verify(tool, never()).updateProject(any(), any());

        AiMessage user = new AiMessage();
        user.setRole("user");
        user.setContent(request);
        AiMessage bot = new AiMessage();
        bot.setRole("assistant");
        bot.setContent(choices);
        String answer = assistant.respond(PROJECT_UPDATE, "1", List.of(user, bot));
        ArgumentCaptor<ProjectListRequest> changed = ArgumentCaptor.forClass(ProjectListRequest.class);
        verify(tool).updateProject(eq(1L), changed.capture());
        assertEquals(LocalDate.of(2026, 11, 1), changed.getValue().startDate());
        assertTrue(answer.contains(first.getProjectName()));
    }

    @Test
    void keepsChoicesAfterInvalidNumber() {
        ProjectList first = project(1L, "코리안리 특종보험 프로젝트");
        ProjectList second = project(2L, "코리안리 계약관리 프로젝트");
        when(tool.findProjects()).thenReturn(List.of(first, second));
        when(tool.updateProject(eq(2L), any())).thenAnswer(call -> fromRequest(2L, call.getArgument(1)));
        AiMessage request = message("user", "코리안리 상태 완료");
        String choices = assistant.respond(PROJECT_UPDATE, request.getContent(), List.of());
        AiMessage prompt = message("assistant", choices);

        String retry = assistant.respond(PROJECT_UPDATE, "9", List.of(request, prompt));
        assertTrue(retry.contains("대상 프로젝트를 번호로 선택해주세요"));
        String result = assistant.respond(PROJECT_UPDATE, "2", List.of(request, prompt, message("user", "9"), message("assistant", retry)));
        verify(tool).updateProject(eq(2L), any());
        assertTrue(result.contains(second.getProjectName()));
    }

    @Test
    void continuesUpdateWhenMissingStatusIsProvidedLater() {
        ProjectList project = project(1L, "코리안리 프로젝트");
        when(tool.findProjects()).thenReturn(List.of(project));
        when(tool.updateProject(eq(1L), any())).thenAnswer(call -> fromRequest(1L, call.getArgument(1)));
        AiMessage request = message("user", "코리안리 프로젝트 상태 변경해줘");
        String prompt = assistant.respond(PROJECT_UPDATE, request.getContent(), List.of());
        assertTrue(prompt.contains("프로젝트 수정에 필요한 정보를"));

        String answer = assistant.respond(PROJECT_UPDATE, "진행중", List.of(request, message("assistant", prompt)));
        ArgumentCaptor<ProjectListRequest> changed = ArgumentCaptor.forClass(ProjectListRequest.class);
        verify(tool).updateProject(eq(1L), changed.capture());
        assertEquals("in_progress", changed.getValue().projectStatus());
        assertTrue(answer.contains("진행중"));
    }

    @Test
    void parsesDateRangeAndRejectsInvalidOrder() {
        ProjectList project = project(1L, "코리안리 프로젝트");
        when(tool.findProjects()).thenReturn(List.of(project));
        when(tool.updateProject(eq(1L), any())).thenAnswer(call -> fromRequest(1L, call.getArgument(1)));

        assistant.respond(PROJECT_UPDATE, "코리안리 일정 2027-01-10 ~ 6/30", List.of());
        ArgumentCaptor<ProjectListRequest> changed = ArgumentCaptor.forClass(ProjectListRequest.class);
        verify(tool).updateProject(eq(1L), changed.capture());
        assertEquals(LocalDate.of(2027, 1, 10), changed.getValue().startDate());
        assertEquals(LocalDate.of(2027, 6, 30), changed.getValue().endDate());

        String invalid = assistant.respond(PROJECT_UPDATE, "코리안리 일정 2027-05-01 ~ 2027-04-01", List.of());
        assertTrue(invalid.contains("종료일은 시작일보다"));
    }

    @Test
    void updatesEnvironmentAndEndDateWithoutChangingOtherFields() {
        ProjectList project = project(1L, "코리안리 프로젝트");
        when(tool.findProjects()).thenReturn(List.of(project));
        when(tool.updateProject(eq(1L), any())).thenAnswer(call -> fromRequest(1L, call.getArgument(1)));

        assistant.respond(PROJECT_UPDATE, "코리안리 분류 파견", List.of());
        assistant.respond(PROJECT_UPDATE, "코리안리 종료일 12-20", List.of());
        ArgumentCaptor<ProjectListRequest> changed = ArgumentCaptor.forClass(ProjectListRequest.class);
        verify(tool, org.mockito.Mockito.times(2)).updateProject(eq(1L), changed.capture());
        assertEquals("dispatch", changed.getAllValues().get(0).workEnvironment());
        assertEquals(project.getEndDate(), changed.getAllValues().get(0).endDate());
        assertEquals("office", changed.getAllValues().get(1).workEnvironment());
        assertEquals(LocalDate.of(2026, 12, 20), changed.getAllValues().get(1).endDate());
    }

    @Test
    void changesWorkEnvironmentEvenWhenProjectNameContainsOldValue() {
        ProjectList project = project(1L, "파견 코리안리 프로젝트");
        project.setWorkEnvironment("dispatch");
        when(tool.findProjects()).thenReturn(List.of(project));
        when(tool.updateProject(eq(1L), any())).thenAnswer(call -> fromRequest(1L, call.getArgument(1)));

        String answer = assistant.respond(PROJECT_UPDATE,
                "파견 코리안리 프로젝트 근무환경을 내근으로 바꿔줘", List.of());
        ArgumentCaptor<ProjectListRequest> changed = ArgumentCaptor.forClass(ProjectListRequest.class);
        verify(tool).updateProject(eq(1L), changed.capture());
        assertEquals("office", changed.getValue().workEnvironment());
        assertEquals(project.getProjectStatus(), changed.getValue().projectStatus());
        assertTrue(answer.contains("근무 환경:** 내근"));
    }

    @Test
    void sameWorkEnvironmentDoesNotReportUpdate() {
        ProjectList project = project(1L, "코리안리 프로젝트");
        when(tool.findProjects()).thenReturn(List.of(project));

        String answer = assistant.respond(PROJECT_UPDATE,
                "코리안리 근무환경 내근", List.of());
        assertTrue(answer.contains("이미 근무 환경이 내근"));
        verify(tool, never()).updateProject(any(), any());
    }

    @Test
    void doesNotGuessMissingProjectOrAmbiguousYear() {
        ProjectList project = project(1L, "코리안리 프로젝트");
        when(tool.findProjects()).thenReturn(List.of(project));
        assertTrue(assistant.respond(PROJECT_UPDATE, "없는회사 시작일 11/1", List.of()).contains("찾을 수 없습니다"));
        assertTrue(assistant.respond(PROJECT_UPDATE, "코리안리 일정 11/1 ~ 2027-02-28", List.of())
                .contains("연도가 모호"));
        verify(tool, never()).updateProject(any(), any());
    }

    private ProjectList project(Long id, String name) {
        ProjectList project = new ProjectList();
        project.setProjectId(id);
        project.setProjectName(name);
        project.setWorkEnvironment("office");
        project.setProjectStatus("planned");
        project.setStartDate(LocalDate.of(2026, 10, 1));
        project.setEndDate(LocalDate.of(2026, 12, 31));
        return project;
    }

    private ProjectList fromRequest(Long id, ProjectListRequest request) {
        ProjectList project = project(id, request.projectName());
        project.setWorkEnvironment(request.workEnvironment());
        project.setProjectStatus(request.projectStatus());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        return project;
    }

    private AiMessage message(String role, String content) {
        AiMessage item = new AiMessage();
        item.setRole(role);
        item.setContent(content);
        return item;
    }
}
