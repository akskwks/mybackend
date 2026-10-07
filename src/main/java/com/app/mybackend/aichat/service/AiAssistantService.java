package com.app.mybackend.aichat.service;

import com.app.mybackend.aichat.entity.AiMessage;
import com.app.mybackend.calendar.dto.CalendarEventRequest;
import com.app.mybackend.calendar.entity.CalendarEvent;
import com.app.mybackend.calendar.service.CalendarEventService;
import com.app.mybackend.memo.entity.Memo;
import com.app.mybackend.memo.service.MemoService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiAssistantService {
    private static final Pattern TIME = Pattern.compile("(오전|오후)?\\s*(\\d{1,2})\\s*시(?:\\s*(\\d{1,2})\\s*분)?");

    private final AiIntentService intentService;
    private final AiPromptService promptService;
    private final OllamaChatGateway ollamaChatGateway;
    private final CalendarEventService calendarEventService;
    private final MemoService memoService;
    private final ProjectWorkAiAssistant projectWorkAssistant;
    private final AiDateExpressionService dateExpressionService;

    public AiAssistantService(
            AiIntentService intentService,
            AiPromptService promptService,
            OllamaChatGateway ollamaChatGateway,
            CalendarEventService calendarEventService,
            MemoService memoService,
            ProjectWorkAiAssistant projectWorkAssistant,
            AiDateExpressionService dateExpressionService
    ) {
        this.intentService = intentService;
        this.promptService = promptService;
        this.ollamaChatGateway = ollamaChatGateway;
        this.calendarEventService = calendarEventService;
        this.memoService = memoService;
        this.projectWorkAssistant = projectWorkAssistant;
        this.dateExpressionService = dateExpressionService;
    }

    public String respond(String message, List<AiMessage> history) {
        AiIntentService.Intent intent = intentService.detect(message, history);
        if (projectWorkAssistant.supports(intent)) {
            return projectWorkAssistant.respond(intent, message, history);
        }
        return switch (intent) {
            case CALENDAR_QUERY -> answerCalendarQuery(message);
            case CALENDAR_CREATE -> createCalendarEvent(message);
            case MEMO_SEARCH -> searchMemos(message);
            case MEMO_SUMMARY -> summarizeMemos(message, history);
            case GENERAL_CHAT -> askOllama(message, history, "");
            default -> throw new IllegalStateException("처리되지 않은 AI 의도입니다: " + intent);
        };
    }

    private String answerCalendarQuery(String message) {
        AiDateExpressionService.DateSelection range = dateExpressionService.resolve(message);
        if (range == null) {
            LocalDate today = LocalDate.now();
            range = new AiDateExpressionService.DateSelection(today, today, "오늘");
        }
        List<CalendarEvent> events = calendarEventService.findBetween(range.start(), range.end());
        if (events.isEmpty()) {
            String label = range.expression();
            if (label.matches("20\\d{2}[-./].*|\\d{1,2}월.*")) label += "에";
            return "%s 예정된 일정이 없습니다.".formatted(label);
        }

        if (events.size() == 1) {
            return "%s 일정은 %s가 예정되어 있습니다.".formatted(
                    range.expression(), formatCalendarEvent(events.get(0), !range.isSingleDay()));
        }
        StringBuilder answer = new StringBuilder(range.expression()).append(" 일정은 다음과 같습니다.\n\n");
        for (CalendarEvent event : events) {
            answer.append("- ").append(formatCalendarEvent(event, !range.isSingleDay())).append('\n');
        }
        return answer.toString().trim();
    }

    private String formatCalendarEvent(CalendarEvent event, boolean includeDate) {
        StringBuilder item = new StringBuilder();
        if (includeDate && event.getEventDate() != null) {
            item.append(event.getEventDate()).append(' ');
        }
        if (event.getStartTime() != null) {
            item.append(event.getStartTime());
            if (event.getEndTime() != null) item.append('-').append(event.getEndTime());
            item.append(' ');
        }
        return item.append(event.getEventTitle()).toString();
    }

    private String createCalendarEvent(String message) {
        LocalDate date = parseDate(message);
        LocalTime startTime = parseTime(message);
        if (date == null && startTime == null) {
            return "일정을 등록하려면 **날짜와 시작 시간**을 알려주세요. 예: `내일 오후 3시에 프로젝트 회의 일정 추가해줘.`";
        }
        if (date == null) {
            return "일정 날짜가 필요합니다. 오늘, 내일 또는 `2026-09-18`처럼 날짜를 알려주세요.";
        }
        if (startTime == null) {
            return "일정 시작 시간이 필요합니다. `오후 3시`처럼 시간을 알려주세요.";
        }

        String title = extractEventTitle(message);
        if (title.isBlank()) {
            return "등록할 일정의 제목을 알려주세요.";
        }

        String category = containsAny(message.toLowerCase(Locale.ROOT), "업무", "회의", "프로젝트")
                ? "work"
                : "personal";
        CalendarEvent event = calendarEventService.create(new CalendarEventRequest(
                title,
                date,
                startTime,
                startTime.plusHours(1),
                category,
                category.equals("work") ? "blue" : "green",
                "AI 채팅에서 등록한 일정"
        ));
        return "일정을 등록했습니다.\n\n- **제목:** %s\n- **날짜:** %s\n- **시간:** %s - %s"
                .formatted(event.getEventTitle(), event.getEventDate(), event.getStartTime(), event.getEndTime());
    }

    private String searchMemos(String message) {
        String keyword = extractMemoKeyword(message);
        AiDateExpressionService.DateSelection date = dateExpressionService.resolve(message);
        List<Memo> memos = memoService.findAll(keyword).stream()
                .filter(memo -> date == null || (memo.getCreatedAt() != null
                        && !memo.getCreatedAt().toLocalDate().isBefore(date.start())
                        && !memo.getCreatedAt().toLocalDate().isAfter(date.end())))
                .filter(memo -> !message.toLowerCase(Locale.ROOT).contains("todo") || "todo".equals(memo.getMemoSort()))
                .filter(memo -> !message.contains("아직") || valueOrEmpty(memo.getMemoCnnt()).contains("data-checked=\"false\""))
                .limit(10)
                .toList();
        if (memos.isEmpty()) {
            return date == null ? "조건에 맞는 메모를 찾지 못했습니다."
                    : "%s 작성된 메모가 없습니다.".formatted(date.expression());
        }

        StringBuilder result = new StringBuilder("관련 메모를 찾았습니다.\n\n");
        for (Memo memo : memos) {
            result.append("- **").append(memo.getMemoTitle()).append("** (`")
                    .append(memo.getMemoSort()).append("`) — ")
                    .append(shorten(stripHtml(memo.getMemoCnnt()), 100)).append('\n');
        }
        return result.toString().trim();
    }

    private String summarizeMemos(String message, List<AiMessage> history) {
        DateTimeRange range = memoRange(message);
        List<Memo> memos = memoService.findUpdatedBetween(range.start(), range.end()).stream()
                .filter(memo -> !message.contains("업무") || "work".equals(memo.getMemoSort()))
                .limit(20)
                .toList();
        if (memos.isEmpty()) return "요약할 메모를 찾지 못했습니다.";

        String data = memos.stream()
                .map(memo -> "[메모: %s / 분류: %s / 수정: %s]\n%s".formatted(
                        memo.getMemoTitle(),
                        memo.getMemoSort(),
                        memo.getUpdatedAt(),
                        stripHtml(memo.getMemoCnnt())
                ))
                .reduce((left, right) -> left + "\n\n" + right)
                .orElse("");
        return askOllama(message, history, "[메모 데이터]\n" + data);
    }

    private String askOllama(String message, List<AiMessage> history, String myAppData) {
        return ollamaChatGateway.generate(
                promptService.systemPrompt(),
                promptService.userPrompt(message, history, myAppData)
        );
    }

    private DateTimeRange memoRange(String message) {
        AiDateExpressionService.DateSelection selection = dateExpressionService.resolve(message);
        LocalDate start = selection == null ? LocalDate.now() : selection.start();
        LocalDate end = selection == null ? start : selection.end();
        return new DateTimeRange(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    private LocalDate parseDate(String message) {
        AiDateExpressionService.DateSelection selection = dateExpressionService.resolve(message);
        return selection != null && selection.isSingleDay() ? selection.start() : null;
    }

    private LocalTime parseTime(String message) {
        Matcher matcher = TIME.matcher(message);
        if (!matcher.find()) return null;
        int hour = Integer.parseInt(matcher.group(2));
        int minute = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
        if ("오후".equals(matcher.group(1)) && hour < 12) hour += 12;
        if ("오전".equals(matcher.group(1)) && hour == 12) hour = 0;
        if (hour > 23 || minute > 59) return null;
        return LocalTime.of(hour, minute);
    }

    private String extractEventTitle(String message) {
        return dateExpressionService.withoutDateExpression(message)
                .replaceAll("(오전|오후)?\\s*\\d{1,2}\\s*시(?:\\s*\\d{1,2}\\s*분)?(?:에)?", "")
                .replaceAll("일정(?:을)?|추가해줘|추가해 주세요|추가|등록해줘|등록해 주세요|등록|잡아줘|만들어줘", "")
                .replaceAll("^[에은는을를과와\\s]+|[.?!\\s]+$", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String extractMemoKeyword(String message) {
        return dateExpressionService.withoutDateExpression(message)
                .replaceAll("작성한|적어놓은|관련해서|관련|TODO|todo", "")
                .replaceAll("메모|중|아직|해야 할|내용|알려줘|알려 주세요|찾아줘|찾아 주세요|찾아|검색해줘|검색|있어|보여줘|조회해줘", "")
                .replaceAll("[?!.]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String stripHtml(String html) {
        if (html == null || html.isBlank()) return "";
        String withBreaks = html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</(p|div|li|h[1-6]|pre)>", "\n");
        return HtmlUtils.htmlUnescape(withBreaks.replaceAll("<[^>]+>", " "))
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private String shorten(String value, int limit) {
        return value.length() <= limit ? value : value.substring(0, limit) + "...";
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    private record DateTimeRange(LocalDateTime start, LocalDateTime end) {
    }
}
