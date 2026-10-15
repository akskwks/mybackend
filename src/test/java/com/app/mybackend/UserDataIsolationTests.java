package com.app.mybackend;

import com.app.mybackend.aichat.repository.AiConversationRepository;
import com.app.mybackend.aichat.repository.AiMessageRepository;
import com.app.mybackend.aichat.service.AiConversationService;
import com.app.mybackend.calendar.dto.CalendarEventRequest;
import com.app.mybackend.calendar.entity.CalendarEvent;
import com.app.mybackend.calendar.repository.CalendarEventRepository;
import com.app.mybackend.calendar.service.CalendarEventService;
import com.app.mybackend.config.CurrentUser;
import com.app.mybackend.memo.repository.MemoRepository;
import com.app.mybackend.memo.dto.MemoRequest;
import com.app.mybackend.memo.entity.Memo;
import com.app.mybackend.memo.service.MemoService;
import com.app.mybackend.work.repository.ProjectListRepository;
import com.app.mybackend.work.repository.WorkListRepository;
import com.app.mybackend.work.service.ProjectListService;
import com.app.mybackend.work.service.WorkFileService;
import com.app.mybackend.work.service.WorkListService;
import com.app.mybackend.work.dto.ProjectListRequest;
import com.app.mybackend.work.entity.ProjectList;
import com.app.mybackend.work.entity.WorkList;
import com.app.mybackend.work.repository.WorkFileRepository;
import com.app.mybackend.work.service.WorkFileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class UserDataIsolationTests {
    private final CurrentUser currentUser = new CurrentUser();

    @BeforeEach
    void signInAsSecondUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(2L, null, List.of()));
    }

    @AfterEach
    void clearSession() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void calendarListsOnlyCurrentUsersEventsAndHidesOtherIds() {
        CalendarEventRepository repository = mock(CalendarEventRepository.class);
        CalendarEventService service = new CalendarEventService(repository, currentUser);

        service.findAll();
        verify(repository).findByUserNumOrderByEventDateAscStartTimeAsc(2L);
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.delete(9L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(repository).findByEventIdAndUserNum(9L, 2L);
    }

    @Test
    void memoSearchAndDetailUseCurrentUser() {
        MemoRepository repository = mock(MemoRepository.class);
        MemoService service = new MemoService(repository, currentUser);

        service.findAll(null, null);
        verify(repository).search(2L, null, null);
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.findById(9L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(repository).findByMemoIdAndUserNum(9L, 2L);
    }

    @Test
    void projectListAndIdAreScopedToCurrentUser() {
        ProjectListRepository projects = mock(ProjectListRepository.class);
        ProjectListService service = new ProjectListService(
                projects, mock(WorkListRepository.class), currentUser);

        service.findAll();
        verify(projects).findByUserNumOrderByUpdatedAtDesc(2L);
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.findById(9L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(projects).findByProjectIdAndUserNum(9L, 2L);
    }

    @Test
    void workListRequiresOwnedProject() {
        ProjectListService projects = mock(ProjectListService.class);
        WorkListService service = new WorkListService(
                mock(WorkListRepository.class), mock(WorkFileService.class), projects);
        when(projects.findById(9L)).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));

        assertThrows(ResponseStatusException.class, () -> service.findAll(9L, null));
        verify(projects).findById(9L);
    }

    @Test
    void aiConversationListAndMessagesAreScopedToCurrentUser() {
        AiConversationRepository conversations = mock(AiConversationRepository.class);
        AiConversationService service = new AiConversationService(
                conversations, mock(AiMessageRepository.class), currentUser);

        service.findAll();
        verify(conversations).findByUserNumOrderByUpdatedAtDesc(2L);
        assertThrows(IllegalArgumentException.class, () -> service.findMessages(9L));
        verify(conversations).findByConversationIdAndUserNum(9L, 2L);
    }

    @Test
    void newRootRecordsBelongToCurrentUser() {
        CalendarEventRepository calendars = mock(CalendarEventRepository.class);
        MemoRepository memos = mock(MemoRepository.class);
        ProjectListRepository projects = mock(ProjectListRepository.class);
        AiConversationRepository conversations = mock(AiConversationRepository.class);

        when(calendars.save(any(CalendarEvent.class))).thenAnswer(call -> call.getArgument(0));
        when(memos.save(any(Memo.class))).thenAnswer(call -> call.getArgument(0));
        when(projects.save(any(ProjectList.class))).thenAnswer(call -> call.getArgument(0));
        when(conversations.save(any(com.app.mybackend.aichat.entity.AiConversation.class)))
                .thenAnswer(call -> call.getArgument(0));

        CalendarEvent event = new CalendarEventService(calendars, currentUser).create(
                new CalendarEventRequest("meeting", LocalDate.now(), null, null, null, null, null));
        Memo memo = new MemoService(memos, currentUser).create(new MemoRequest("note", "body", "general"));
        ProjectList project = new ProjectListService(projects, mock(WorkListRepository.class), currentUser)
                .create(new ProjectListRequest("office", "project", "planned", LocalDate.now(), LocalDate.now()));
        var conversation = new AiConversationService(conversations, mock(AiMessageRepository.class), currentUser)
                .create("chat");

        assertEquals(2L, event.getUserNum());
        assertEquals(2L, memo.getUserNum());
        assertEquals(2L, project.getUserNum());
        assertEquals(2L, conversation.getUserNum());
    }

    @Test
    void workFilesRequireOwnershipOfTheirParentProject() {
        WorkListRepository works = mock(WorkListRepository.class);
        ProjectListService projects = mock(ProjectListService.class);
        WorkFileService files = new WorkFileService(
                mock(WorkFileRepository.class), mock(WorkFileStorageService.class), works, projects);
        WorkList work = new WorkList();
        work.setProjectId(9L);
        when(works.findById(3L)).thenReturn(Optional.of(work));
        when(projects.findById(9L)).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));

        assertThrows(ResponseStatusException.class, () -> files.findAll(3L));
        verify(projects).findById(9L);
    }
}
