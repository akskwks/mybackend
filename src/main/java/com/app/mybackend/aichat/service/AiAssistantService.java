package com.app.mybackend.aichat.service;

import com.app.mybackend.aichat.entity.AiMessage;
import com.app.mybackend.calendar.dto.CalendarEventRequest;
import com.app.mybackend.calendar.entity.CalendarEvent;
import com.app.mybackend.calendar.service.CalendarEventService;
import com.app.mybackend.memo.entity.Memo;
import com.app.mybackend.memo.service.MemoService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiAssistantService {
    private static final Pattern ISO_DATE = Pattern.compile("(20\\d{2})[-./](\\d{1,2})[-./](\\d{1,2})");
    private static final Pattern MONTH_DAY = Pattern.compile("(\\d{1,2})월\\s*(\\d{1,2})일");
    private static final Pattern TIME = Pattern.compile("(오전|오후)?\\s*(\\d{1,2})\\s*시(?:\\s*(\\d{1,2})\\s*분)?");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AiIntentService intentService;
    private final AiPromptService promptService;
    private final OllamaChatGateway ollamaChatGateway;
    private final CalendarEventService calendarEventService;
    private final MemoService memoService;

    public AiAssistantService(
            AiIntentService intentService,
            AiPromptService promptService,
            OllamaChatGateway ollamaChatGateway,
            CalendarEventService calendarEventService,
            MemoService memoService
    ) {
        this.intentService = intentService;
        this.promptService = promptService;
        this.ollamaChatGateway = ollamaChatGateway;
        this.calendarEventService = calendarEventService;
        this.memoService = memoService;
    }

    public String respond(String message, List<AiMessage> history) {
        return switch (intentService.detect(message)) {
            case CALENDAR_QUERY -> answerCalendarQuery(message, history);
            case CALENDAR_CREATE -> createCalendarEvent(message);
            case MEMO_SEARCH -> searchMemos(message);
            case MEMO_SUMMARY -> summarizeMemos(message, history);
            case GENERAL_CHAT -> askOllama(message, history, "");
        };
    }

    private String answerCalendarQuery(String message, List<AiMessage> history) {
        DateRange range = calendarRange(message);
        List<CalendarEvent> events = calendarEventService.findBetween(range.start(), range.end());
        if (events.isEmpty()) {
            return "%s부터 %s까지 등록된 일정이 없습니다."
                    .formatted(range.start().format(DATE_FORMAT), range.end().format(DATE_FORMAT));
        }

        String data = events.stream()
                .map(event -> "- %s %s-%s | %s | %s | %s".formatted(
                        event.getEventDate(),
                        event.getStartTime(),
                        event.getEndTime(),
                        event.getEventTitle(),
                        valueOrEmpty(event.getEventCatg()),
                        valueOrEmpty(event.getEvent_dsc())
                ))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
        return askOllama(message, history, "[캘린더 조회 결과]\n" + data);
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
        List<Memo> memos = memoService.findAll(keyword).stream()
                .filter(memo -> !message.toLowerCase(Locale.ROOT).contains("todo") || "todo".equals(memo.getMemoSort()))
                .filter(memo -> !message.contains("아직") || valueOrEmpty(memo.getMemoCnnt()).contains("data-checked=\"false\""))
                .limit(10)
                .toList();
        if (memos.isEmpty()) return "조건에 맞는 메모를 찾지 못했습니다.";

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

    private DateRange calendarRange(String message) {
        LocalDate today = LocalDate.now();
        if (message.contains("내일")) return new DateRange(today.plusDays(1), today.plusDays(1));
        if (message.contains("이번 주") || message.contains("이번주")) {
            LocalDate monday = today.with(DayOfWeek.MONDAY);
            return new DateRange(monday, monday.plusDays(6));
        }
        LocalDate explicit = parseDate(message);
        LocalDate target = explicit == null ? today : explicit;
        return new DateRange(target, target);
    }

    private DateTimeRange memoRange(String message) {
        LocalDate today = LocalDate.now();
        if (message.contains("지난주") || message.contains("지난 주")) {
            LocalDate thisMonday = today.with(DayOfWeek.MONDAY);
            return new DateTimeRange(thisMonday.minusWeeks(1).atStartOfDay(), thisMonday.atStartOfDay());
        }
        if (message.contains("이번주") || message.contains("이번 주")) {
            LocalDate monday = today.with(DayOfWeek.MONDAY);
            return new DateTimeRange(monday.atStartOfDay(), monday.plusWeeks(1).atStartOfDay());
        }
        return new DateTimeRange(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    private LocalDate parseDate(String message) {
        LocalDate today = LocalDate.now();
        if (message.contains("내일")) return today.plusDays(1);
        if (message.contains("오늘")) return today;

        Matcher isoMatcher = ISO_DATE.matcher(message);
        if (isoMatcher.find()) {
            return LocalDate.of(
                    Integer.parseInt(isoMatcher.group(1)),
                    Integer.parseInt(isoMatcher.group(2)),
                    Integer.parseInt(isoMatcher.group(3))
            );
        }
        Matcher monthDayMatcher = MONTH_DAY.matcher(message);
        if (monthDayMatcher.find()) {
            return LocalDate.of(
                    today.getYear(),
                    Integer.parseInt(monthDayMatcher.group(1)),
                    Integer.parseInt(monthDayMatcher.group(2))
            );
        }
        return null;
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
        return message
                .replaceAll("오늘|내일", "")
                .replaceAll("20\\d{2}[-./]\\d{1,2}[-./]\\d{1,2}", "")
                .replaceAll("\\d{1,2}월\\s*\\d{1,2}일", "")
                .replaceAll("(오전|오후)?\\s*\\d{1,2}\\s*시(?:\\s*\\d{1,2}\\s*분)?(?:에)?", "")
                .replaceAll("일정(?:을)?|추가해줘|추가해 주세요|추가|등록해줘|등록해 주세요|등록|잡아줘|만들어줘", "")
                .replaceAll("^[에은는을를과와\\s]+|[.?!\\s]+$", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String extractMemoKeyword(String message) {
        return message
                .replaceAll("오늘|지난주|지난 주|이번주|이번 주|작성한|적어놓은|관련해서|관련|TODO|todo", "")
                .replaceAll("메모|중|아직|해야 할|내용|찾아줘|찾아 주세요|찾아|검색해줘|검색|있어|보여줘", "")
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

    private record DateRange(LocalDate start, LocalDate end) {
    }

    private record DateTimeRange(LocalDateTime start, LocalDateTime end) {
    }
}
