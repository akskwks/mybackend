package com.app.mybackend;

import com.app.mybackend.aichat.entity.AiMessage;
import com.app.mybackend.aichat.service.AiIntentService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.app.mybackend.aichat.service.AiIntentService.Intent;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AiIntentServiceTests {

    private final AiIntentService service = new AiIntentService();

    @Test
    void detectsProjectRequestsByRequestedScope() {
        assertEquals(Intent.PROJECT_LIST_QUERY, service.detect("현재 등록된 프로젝트 목록 보여줘"));
        assertEquals(Intent.PROJECT_DETAIL_QUERY, service.detect("MyApp 프로젝트 정보 알려줘"));
        assertEquals(Intent.PROJECT_PERIOD_QUERY, service.detect("MyApp 프로젝트 기간 알려줘"));
        assertEquals(Intent.PROJECT_WORK_QUERY, service.detect("MyApp 프로젝트에는 어떤 업무들이 등록되어 있어?"));
    }

    @Test
    void distinguishesRegisteredWorkQueryFromCreateCommand() {
        assertEquals(Intent.PROJECT_WORK_QUERY, service.detect("MyApp 프로젝트에 등록된 업무 알려줘"));
        assertEquals(Intent.WORK_CREATE, service.detect("MyApp 프로젝트에 개발 업무 추가해줘"));
        assertEquals(Intent.PROJECT_CREATE, service.detect("업무 관리 프로젝트 추가해줘"));
    }

    @Test
    void detectsProjectAndWorkMutations() {
        assertEquals(Intent.PROJECT_UPDATE, service.detect("MyApp 프로젝트 종료일을 변경해줘"));
        assertEquals(Intent.PROJECT_DELETE, service.detect("MyApp 테스트 프로젝트 삭제해줘"));
        assertEquals(Intent.WORK_UPDATE, service.detect("MyApp 프로젝트의 개발 업무 상태를 종료로 변경해줘"));
        assertEquals(Intent.WORK_DELETE, service.detect("MyApp 프로젝트에서 테스트 업무 삭제해줘"));
    }

    @Test
    void continuesPendingCreateRequest() {
        AiMessage assistant = new AiMessage();
        assistant.setRole("assistant");
        assistant.setContent("프로젝트 등록에 필요한 정보를 더 알려주세요: 진행 상태.");

        assertEquals(
                Intent.PROJECT_CREATE,
                service.detect("진행중이야", List.of(assistant))
        );
    }

    @Test
    void recognizesShortCalendarAndMemoQueries() {
        assertEquals(Intent.CALENDAR_QUERY, service.detect("내일 일정"));
        assertEquals(Intent.CALENDAR_QUERY, service.detect("모레 일정"));
        assertEquals(Intent.CALENDAR_QUERY, service.detect("일주일 뒤 일정"));
        assertEquals(Intent.MEMO_SEARCH, service.detect("어제 메모"));
        assertEquals(Intent.MEMO_SEARCH, service.detect("그저께 작성한 메모"));
        assertEquals(Intent.MEMO_SEARCH, service.detect("지난주 메모"));
        assertEquals(Intent.MEMO_SEARCH, service.detect("Docker 관련 메모"));
        assertEquals(Intent.MEMO_SEARCH, service.detect("TODO 메모"));
        assertEquals(Intent.MEMO_SEARCH, service.detect("지난주 프로젝트 관련 메모"));
        assertEquals(Intent.CALENDAR_QUERY, service.detect("내일 프로젝트 회의 일정"));
    }

    @Test
    void recognizesProjectScopeWithoutCommandVerbs() {
        assertEquals(Intent.PROJECT_LIST_QUERY, service.detect("이번 주 프로젝트"));
        assertEquals(Intent.PROJECT_PERIOD_QUERY, service.detect("MyApp 프로젝트 기간"));
        assertEquals(Intent.PROJECT_WORK_QUERY, service.detect("MyApp 주요 업무"));
        assertEquals(Intent.GENERAL_CHAT, service.detect("Spring Boot JPA가 뭐야?"));
    }
}
