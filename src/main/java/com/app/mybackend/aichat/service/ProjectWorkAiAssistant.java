package com.app.mybackend.aichat.service;

import com.app.mybackend.aichat.entity.AiMessage;
import com.app.mybackend.aichat.service.AiIntentService.Intent;
import com.app.mybackend.work.dto.ProjectListRequest;
import com.app.mybackend.work.dto.WorkListRequest;
import com.app.mybackend.work.entity.ProjectList;
import com.app.mybackend.work.entity.WorkList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ProjectWorkAiAssistant {

    private static final Set<Intent> SUPPORTED_INTENTS = EnumSet.of(
            Intent.PROJECT_LIST_QUERY,
            Intent.PROJECT_DETAIL_QUERY,
            Intent.PROJECT_PERIOD_QUERY,
            Intent.PROJECT_WORK_QUERY,
            Intent.PROJECT_CREATE,
            Intent.PROJECT_UPDATE,
            Intent.PROJECT_DELETE,
            Intent.WORK_CREATE,
            Intent.WORK_UPDATE,
            Intent.WORK_DELETE
    );
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(?<![0-9])(?:(20\\d{2})[-./]|(20\\d{2})년\\s*)?(\\d{1,2})(?:[-./]|월\\s*)(\\d{1,2})(?:일)?(?![0-9])"
    );
    private static final Pattern NUMBERED_PROJECT = Pattern.compile("(?m)^(\\d+)\\. (.+)$");
    private static final Pattern PROJECT_FIELD = Pattern.compile("상태|진행사항|진행\\s*상태|분류|근무\\s*환경|시작일|종료일|일정|기간");
    private static final Pattern ENVIRONMENT_FIELD = Pattern.compile("근무\\s*환경|분류");
    private static final Pattern ENVIRONMENT_VALUE = Pattern.compile("내근|파견");
    private static final Pattern PROJECT_NAME_CHANGE = Pattern.compile(
            "프로젝트(?:명| 이름)(?:을|를)?\\s*(.+?)(?:으로|로)\\s*(?:변경|수정|바꿔)"
    );
    private static final Pattern WORK_NAME_CHANGE = Pattern.compile(
            "업무(?:명| 제목)(?:을|를)?\\s*(.+?)(?:으로|로)\\s*(?:변경|수정|바꿔)"
    );
    private static final Pattern WORK_CONTENT_CHANGE = Pattern.compile(
            "업무\\s*내용(?:을|를)?\\s*(.+?)(?:으로|로)\\s*(?:변경|수정|바꿔)"
    );
    private static final String WORK_TEMPLATE = """
            <table><thead><tr><th><p>구분</p></th><th><p>내용</p></th></tr></thead>
            <tbody><tr><td><p>주요 작업 내용</p></td><td><p>내용을 입력하세요.</p></td></tr>
            <tr><td><p>이슈 / 특이사항</p></td><td><p>내용을 입력하세요.</p></td></tr>
            <tr><td><p>다음 작업</p></td><td><p>내용을 입력하세요.</p></td></tr></tbody></table>
            """;

    private final ProjectWorkAiTool tool;
    private final AiDateExpressionService dateExpressionService;

    public boolean supports(Intent intent) {
        return SUPPORTED_INTENTS.contains(intent);
    }

    public String respond(Intent intent, String message, List<AiMessage> history) {
        try {
            return switch (intent) {
                case PROJECT_LIST_QUERY -> listProjects(message);
                case PROJECT_DETAIL_QUERY -> projectDetail(message);
                case PROJECT_PERIOD_QUERY -> projectPeriod(message);
                case PROJECT_WORK_QUERY -> projectWorks(message);
                case PROJECT_CREATE -> createProject(withPendingContext(message, history, "프로젝트 등록에 필요한 정보를"));
                case PROJECT_UPDATE -> updateProject(message, history);
                case PROJECT_DELETE -> deleteProject(withPendingContext(message, history, "프로젝트 삭제 대상을"));
                case WORK_CREATE -> createWork(withPendingContext(message, history, "업무 등록에 필요한 정보를"));
                case WORK_UPDATE -> updateWork(withPendingContext(message, history, "업무 수정 대상을"));
                case WORK_DELETE -> deleteWork(withPendingContext(message, history, "업무 삭제 대상을"));
                default -> throw new IllegalArgumentException("지원하지 않는 프로젝트/업무 요청입니다.");
            };
        } catch (ResponseStatusException exception) {
            return exception.getReason() == null ? "요청을 처리하지 못했습니다." : exception.getReason();
        } catch (IllegalArgumentException exception) {
            return exception.getMessage() == null ? "요청 내용을 확인해 주세요." : exception.getMessage();
        }
    }

    private String listProjects(String message) {
        List<ProjectList> projects = tool.findProjects();
        AiDateExpressionService.DateSelection period = dateExpressionService.resolve(message);
        if (period != null) {
            projects = projects.stream()
                    .filter(project -> !project.getEndDate().isBefore(period.start())
                            && !project.getStartDate().isAfter(period.end()))
                    .toList();
        }
        if (projects.isEmpty()) {
            return period == null ? "현재 등록된 프로젝트가 없습니다."
                    : "%s에 해당하는 프로젝트가 없습니다.".formatted(period.expression());
        }

        StringBuilder answer = new StringBuilder(period == null
                ? "현재 등록된 프로젝트 목록입니다.\n"
                : "%s에 해당하는 프로젝트 목록입니다.\n".formatted(period.expression()));
        for (ProjectList project : projects) {
            answer.append("\n**").append(project.getProjectName()).append("**\n")
                    .append("- 프로젝트 기간: ").append(project.getStartDate()).append(" ~ ")
                    .append(project.getEndDate()).append('\n')
                    .append("- 등록된 업무: ").append(tool.countWorks(project.getProjectId())).append("건\n");
        }
        return answer.toString().trim();
    }

    private String projectDetail(String message) {
        ProjectSelection selection = resolveProject(message);
        if (!selection.resolved()) return selection.message();

        ProjectList project = selection.project();
        List<WorkList> works = tool.findWorks(project.getProjectId());
        StringBuilder answer = new StringBuilder("**").append(project.getProjectName()).append("**\n\n")
                .append("- 프로젝트 기간: ").append(project.getStartDate()).append(" ~ ")
                .append(project.getEndDate()).append('\n')
                .append("- 근무 환경: ").append(environmentLabel(project.getWorkEnvironment())).append('\n')
                .append("- 진행 상태: ").append(statusLabel(project.getProjectStatus())).append('\n')
                .append("- 주요 업무");
        if (works.isEmpty()) return answer.append("\n  - 등록된 업무가 없습니다.").toString();
        works.stream().limit(10).forEach(work -> answer.append("\n  - ").append(work.getWorkTitle()));
        if (works.size() > 10) answer.append("\n  - 외 ").append(works.size() - 10).append("건");
        return answer.toString();
    }

    private String projectPeriod(String message) {
        ProjectSelection selection = resolveProject(message);
        if (!selection.resolved()) return selection.message();
        ProjectList project = selection.project();
        return "%s 프로젝트 기간은 %s부터 %s까지입니다."
                .formatted(project.getProjectName(), project.getStartDate(), project.getEndDate());
    }

    private String projectWorks(String message) {
        ProjectSelection selection = resolveProject(message);
        if (!selection.resolved()) return selection.message();
        ProjectList project = selection.project();
        List<WorkList> works = tool.findWorks(project.getProjectId());
        AiDateExpressionService.DateSelection period = dateExpressionService.resolve(message);
        if (period != null) {
            works = works.stream()
                    .filter(work -> work.getWorkDate() != null
                            && !work.getWorkDate().isBefore(period.start())
                            && !work.getWorkDate().isAfter(period.end()))
                    .toList();
        }
        if (works.isEmpty()) return "%s 프로젝트에 등록된 업무가 없습니다.".formatted(project.getProjectName());

        StringBuilder answer = new StringBuilder(project.getProjectName())
                .append(" 프로젝트에 등록된 주요 업무입니다.\n");
        works.stream().limit(20).forEach(work -> answer.append("\n- **").append(work.getWorkTitle()).append("**")
                .append(" · ").append(work.getWorkDate())
                .append(" · ").append(statusLabel(work.getWorkStatus())));
        if (works.size() > 20) answer.append("\n- 외 ").append(works.size() - 20).append("건");
        return answer.toString();
    }

    private String createProject(String message) {
        String name = extractCreatedProjectName(message);
        String environment = parseEnvironment(message);
        String status = parseStatus(message);
        if (status == null) status = "planned";
        List<DateInput> dates = findDates(message);
        List<String> missing = new ArrayList<>();
        if (name == null) missing.add("프로젝트명");
        if (environment == null) missing.add("근무 환경(내근/파견)");
        if (dates.size() < 2) missing.add("시작일과 종료일");
        if (!missing.isEmpty()) {
            return "프로젝트 등록에 필요한 정보를 더 알려주세요: **%s**."
                    .formatted(String.join(", ", missing));
        }

        Integer sharedYear = dates.get(0).year() != null ? dates.get(0).year() : dates.get(1).year();
        if (sharedYear == null) sharedYear = LocalDate.now().getYear();
        LocalDate startDate = dates.get(0).toDate(sharedYear);
        LocalDate endDate = dates.get(1).toDate(sharedYear);
        if (startDate.isAfter(endDate)) return "종료일은 시작일보다 빠를 수 없습니다. 기간을 확인해 주세요.";
        ProjectList saved = tool.createProject(new ProjectListRequest(environment, name, status, startDate, endDate));
        return "프로젝트를 등록했습니다.\n\n- **프로젝트명:** %s\n- **근무 환경:** %s\n- **진행 상태:** %s\n- **기간:** %s ~ %s"
                .formatted(saved.getProjectName(), environmentLabel(saved.getWorkEnvironment()),
                        statusLabel(saved.getProjectStatus()), saved.getStartDate(), saved.getEndDate());
    }

    private String updateProject(String message, List<AiMessage> history) {
        PendingProjectChoice choice = resolvePendingProjectChoice(message, history);
        if (choice != null && !choice.selection().resolved()) return choice.selection().message();
        if (choice != null) message = choice.request();
        else {
            message = withPendingContext(message, history, "프로젝트 수정 대상을");
            message = withPendingContext(message, history, "프로젝트 수정에 필요한 정보를");
        }
        ProjectSelection selection = choice == null ? resolveProject(message) : choice.selection();
        if (!selection.resolved()) return "프로젝트 수정 대상을 확인해 주세요. " + selection.message();
        ProjectList project = selection.project();

        boolean environmentRequested = message.contains("분류") || message.matches("(?s).*근무\\s*환경.*");
        boolean statusRequested = message.contains("상태") || message.contains("진행사항");
        String environment = environmentRequested ? parseUpdatedEnvironment(message) : null;
        String status = statusRequested ? parseStatus(message) : null;
        if (environmentRequested && environment == null) return "프로젝트 수정에 필요한 정보를 더 알려주세요: 변경할 분류(내근/파견).";
        if (statusRequested && status == null) return "프로젝트 수정에 필요한 정보를 더 알려주세요: 변경할 상태(예정/진행중/완료/보류).";
        String changedName = findGroup(PROJECT_NAME_CHANGE, message);
        LocalDate startDate = project.getStartDate();
        LocalDate endDate = project.getEndDate();
        boolean startRequested = message.contains("시작일");
        boolean endRequested = message.contains("종료일");
        boolean periodRequested = !startRequested && !endRequested
                && (message.contains("일정") || message.contains("기간"));
        if (startRequested || endRequested || periodRequested) {
            List<DateInput> dates = findDates(message);
            int required = periodRequested || startRequested && endRequested ? 2 : 1;
            if (dates.size() < required) return "프로젝트 수정에 필요한 정보를 더 알려주세요: 변경할 %s."
                    .formatted(required == 2 ? "시작일과 종료일" : startRequested ? "시작일" : "종료일");
            if (periodRequested && dates.get(0).year() == null && dates.get(1).year() != null
                    && project.getStartDate().getYear() != dates.get(1).year()) {
                return "시작일의 연도가 모호합니다. 시작일과 종료일의 연도를 포함해 다시 요청해 주세요.";
            }
            if (startRequested || periodRequested) {
                Integer year = periodRequested && dates.get(0).year() == null && dates.get(1).year() != null
                        ? dates.get(1).year() : project.getStartDate().getYear();
                startDate = dates.get(0).toDate(year);
            }
            if (endRequested || periodRequested) {
                DateInput endInput = dates.get(required - 1);
                Integer year = periodRequested && endInput.year() == null && dates.get(0).year() != null
                        ? dates.get(0).year() : project.getEndDate().getYear();
                endDate = endInput.toDate(year);
            }
        }
        if (startDate.isAfter(endDate)) return "종료일은 시작일보다 빠를 수 없습니다. 기간을 확인해 주세요.";
        boolean changed = (environment != null && !environment.equals(project.getWorkEnvironment()))
                || (status != null && !status.equals(project.getProjectStatus())) || changedName != null
                || !startDate.equals(project.getStartDate()) || !endDate.equals(project.getEndDate());
        if (!changed) return environmentRequested && environment != null
                ? "이미 근무 환경이 %s입니다.".formatted(environmentLabel(project.getWorkEnvironment()))
                : "변경할 항목과 현재와 다른 값을 알려주세요.";

        ProjectList saved = tool.updateProject(project.getProjectId(), new ProjectListRequest(
                environment == null ? project.getWorkEnvironment() : environment,
                changedName == null ? project.getProjectName() : changedName,
                status == null ? project.getProjectStatus() : status,
                startDate,
                endDate
        ));
        return "%s 프로젝트를 수정했습니다.\n\n- **근무 환경:** %s\n- **진행 상태:** %s\n- **기간:** %s ~ %s"
                .formatted(saved.getProjectName(), environmentLabel(saved.getWorkEnvironment()),
                        statusLabel(saved.getProjectStatus()), saved.getStartDate(), saved.getEndDate());
    }

    private String deleteProject(String message) {
        ProjectSelection selection = resolveProject(message);
        if (!selection.resolved()) return "프로젝트 삭제 대상을 확인해 주세요. " + selection.message();
        ProjectList project = selection.project();
        tool.deleteProject(project.getProjectId());
        return "%s 프로젝트를 삭제했습니다.".formatted(project.getProjectName());
    }

    private String createWork(String message) {
        ProjectSelection projectSelection = resolveProject(message);
        if (!projectSelection.resolved()) {
            return "업무 등록에 필요한 정보를 더 알려주세요: **대상 프로젝트명**. "
                    + projectSelection.message();
        }
        String title = extractRequestedWorkTitle(message);
        String status = parseStatus(message);
        LocalDate workDate = parseWorkDate(message);
        List<String> missing = new ArrayList<>();
        if (title == null) missing.add("업무 제목");
        if (workDate == null) missing.add("업무일자");
        if (status == null) missing.add("진행 상태(예정/진행중/종료/보류)");
        if (!missing.isEmpty()) {
            return "업무 등록에 필요한 정보를 더 알려주세요: **%s**."
                    .formatted(String.join(", ", missing));
        }

        ProjectList project = projectSelection.project();
        WorkList saved = tool.createWork(new WorkListRequest(
                project.getProjectId(), workDate, title, WORK_TEMPLATE, status
        ));
        return "%s 프로젝트에 업무를 등록했습니다.\n\n- **업무:** %s\n- **업무일자:** %s\n- **진행 상태:** %s"
                .formatted(project.getProjectName(), saved.getWorkTitle(), saved.getWorkDate(),
                        statusLabel(saved.getWorkStatus()));
    }

    private String updateWork(String message) {
        ProjectSelection projectSelection = resolveProject(message);
        if (!projectSelection.resolved()) return "업무 수정 대상을 확인해 주세요. " + projectSelection.message();
        ProjectList project = projectSelection.project();
        WorkSelection workSelection = resolveWork(project, message);
        if (!workSelection.resolved()) return "업무 수정 대상을 확인해 주세요. " + workSelection.message();
        WorkList work = workSelection.work();

        String status = parseStatus(message);
        LocalDate date = parseWorkDate(message);
        String changedName = findGroup(WORK_NAME_CHANGE, message);
        String changedContent = findGroup(WORK_CONTENT_CHANGE, message);
        boolean changed = status != null || date != null || changedName != null || changedContent != null;
        if (!changed) return "수정할 업무 항목과 변경할 값을 알려주세요.";

        WorkList saved = tool.updateWork(project.getProjectId(), work.getWorkId(), new WorkListRequest(
                project.getProjectId(),
                date == null ? work.getWorkDate() : date,
                changedName == null ? work.getWorkTitle() : changedName,
                changedContent == null
                        ? work.getWorkCnnt()
                        : "<p>" + HtmlUtils.htmlEscape(changedContent) + "</p>",
                status == null ? work.getWorkStatus() : status
        ));
        return "%s 프로젝트의 '%s' 업무를 수정했습니다.\n\n- **업무일자:** %s\n- **진행 상태:** %s"
                .formatted(project.getProjectName(), saved.getWorkTitle(), saved.getWorkDate(),
                        statusLabel(saved.getWorkStatus()));
    }

    private String deleteWork(String message) {
        ProjectSelection projectSelection = resolveProject(message);
        if (!projectSelection.resolved()) return "업무 삭제 대상을 확인해 주세요. " + projectSelection.message();
        ProjectList project = projectSelection.project();
        WorkSelection workSelection = resolveWork(project, message);
        if (!workSelection.resolved()) return "업무 삭제 대상을 확인해 주세요. " + workSelection.message();
        WorkList work = workSelection.work();
        tool.deleteWork(project.getProjectId(), work.getWorkId());
        return "%s 프로젝트에서 '%s' 업무를 삭제했습니다."
                .formatted(project.getProjectName(), work.getWorkTitle());
    }

    private ProjectSelection resolveProject(String message) {
        List<ProjectList> projects = tool.findProjects();
        if (projects.isEmpty()) return new ProjectSelection(null, "등록된 프로젝트가 없습니다.");
        String normalizedMessage = normalize(message);
        List<ProjectList> matches = projects.stream()
                .filter(project -> normalizedMessage.contains(normalize(project.getProjectName())))
                .toList();
        if (!matches.isEmpty()) {
            int longest = matches.stream().map(ProjectList::getProjectName).mapToInt(String::length).max().orElse(0);
            List<ProjectList> exact = matches.stream().filter(project -> project.getProjectName().length() == longest).toList();
            if (exact.size() == 1) return new ProjectSelection(exact.get(0), null);
        }
        String keyword = extractProjectKeyword(message);
        if (keyword == null) return new ProjectSelection(null, "프로젝트명 키워드를 알려주세요.");
        String normalizedKeyword = normalize(keyword);
        matches = projects.stream()
                .filter(project -> normalize(project.getProjectName()).contains(normalizedKeyword))
                .toList();
        if (matches.isEmpty()) return new ProjectSelection(null,
                "'%s'가 포함된 프로젝트를 찾을 수 없습니다.".formatted(keyword));
        if (matches.size() == 1) return new ProjectSelection(matches.get(0), null);
        StringBuilder answer = new StringBuilder("'%s'가 포함된 프로젝트가 여러 개 있습니다.\n대상 프로젝트를 선택해주세요.\n"
                .formatted(keyword));
        for (int index = 0; index < matches.size(); index++) {
            answer.append('\n').append(index + 1).append(". ").append(matches.get(index).getProjectName());
        }
        return new ProjectSelection(null, answer.toString());
    }

    private PendingProjectChoice resolvePendingProjectChoice(String message, List<AiMessage> history) {
        if (!message.trim().matches("[1-9][0-9]*") || history == null || history.isEmpty()) return null;
        for (int index = history.size() - 1; index >= 0; index--) {
            AiMessage answer = history.get(index);
            if (!"assistant".equals(answer.getRole())) continue;
            if (!answer.getContent().contains("프로젝트 수정 대상을 확인해 주세요.")
                    || !answer.getContent().contains("대상 프로젝트를 선택해주세요.")) return null;
            int selected = Integer.parseInt(message.trim());
            Matcher matcher = NUMBERED_PROJECT.matcher(answer.getContent());
            String name = null;
            while (matcher.find()) {
                if (Integer.parseInt(matcher.group(1)) == selected) name = matcher.group(2).trim();
            }
            if (name == null) return new PendingProjectChoice(
                    new ProjectSelection(null, answer.getContent() + "\n목록에 있는 번호를 선택해 주세요."), message);
            String selectedName = name;
            List<ProjectList> projects = tool.findProjects().stream()
                    .filter(project -> project.getProjectName().equals(selectedName)).toList();
            if (projects.size() != 1) return new PendingProjectChoice(
                    new ProjectSelection(null, "선택한 프로젝트를 다시 확인해 주세요."), message);
            for (int previous = index - 1; previous >= 0; previous--) {
                AiMessage request = history.get(previous);
                if ("user".equals(request.getRole()) && !request.getContent().trim().matches("[1-9][0-9]*")) {
                    return new PendingProjectChoice(new ProjectSelection(projects.get(0), null), request.getContent());
                }
            }
            return null;
        }
        return null;
    }

    private WorkSelection resolveWork(ProjectList project, String message) {
        List<WorkList> works = tool.findWorks(project.getProjectId());
        if (works.isEmpty()) return new WorkSelection(null, "해당 프로젝트에 등록된 업무가 없습니다.");
        String normalizedMessage = normalize(message);
        List<WorkList> matches = works.stream()
                .filter(work -> normalizedMessage.contains(normalize(work.getWorkTitle())))
                .toList();
        if (matches.isEmpty()) {
            String keyword = extractRequestedWorkTitle(message);
            if (keyword != null) {
                String normalizedKeyword = normalize(keyword);
                matches = works.stream()
                        .filter(work -> normalize(work.getWorkTitle()).contains(normalizedKeyword))
                        .toList();
            }
        }
        if (matches.isEmpty()) return new WorkSelection(null, "지정한 프로젝트에서 요청한 업무를 찾지 못했습니다.");
        if (matches.size() > 1) {
            return new WorkSelection(null, "유사한 업무가 여러 개 있습니다: %s. 정확한 업무명을 알려주세요."
                    .formatted(String.join(", ", matches.stream().map(WorkList::getWorkTitle).toList())));
        }
        return new WorkSelection(matches.get(0), null);
    }

    private String withPendingContext(String message, List<AiMessage> history, String marker) {
        if (history == null || history.isEmpty()) return message;
        for (int index = history.size() - 1; index >= 0; index--) {
            AiMessage item = history.get(index);
            if (!"assistant".equals(item.getRole())) continue;
            if (!item.getContent().contains(marker)) return message;
            for (int previous = index - 1; previous >= 0; previous--) {
                AiMessage request = history.get(previous);
                if ("user".equals(request.getRole())) return request.getContent() + "\n" + message;
            }
            return message;
        }
        return message;
    }

    private String extractCreatedProjectName(String message) {
        String value = DATE_PATTERN.matcher(message).replaceAll(" ")
                .replaceAll("(?i)\\b(?:office|dispatch|planned|in_progress|completed|on_hold)\\b", " ")
                .replaceAll("근무\\s*환경|프로젝트명|프로젝트\\s*이름|분류|진행\\s*상태|진행사항|상태", " ")
                .replaceAll("내근|파견|진행\\s*중|완료|종료|보류|예정", " ")
                .replaceAll("추가\\s*정보|정보|새로운|새", " ")
                .replaceAll("프로젝트(?:로|를|을)?|추가|등록|생성|만들어|해주세요|해줘|기간|시작일|종료일", " ")
                .replaceAll("부터|까지|이고|이며|이야|입니다|으로|라는|이라는|[,:~]", " ")
                .replaceAll("(?m)(^|\\s)(?:은|는|로|을|를|이|가|및)(?=\\s|$)", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return value.isBlank() ? null : value;
    }

    private String extractProjectKeyword(String message) {
        Matcher field = PROJECT_FIELD.matcher(message);
        if (field.find() && field.start() > 0) {
            String keyword = message.substring(0, field.start())
                    .replaceAll("프로젝트(?:의|에|에서)?\\s*$", "")
                    .replaceAll("[의을를은는\\s]+$", "")
                    .trim();
            if (!keyword.isBlank()) return keyword;
        }
        int projectIndex = message.indexOf("프로젝트");
        String value;
        if (projectIndex > 0) {
            value = message.substring(0, projectIndex);
        } else {
            int particle = message.indexOf("에서");
            if (particle <= 0) return null;
            value = message.substring(0, particle);
        }
        value = value.replaceAll("^(?:현재|등록된|진행하고 있는|특정)\\s*", "")
                .replaceAll(".*프로젝트로\\s*", "")
                .replaceAll("[의을를은는\\s]+$", "")
                .trim();
        return value.isBlank() ? null : value;
    }

    private String extractRequestedWorkTitle(String message) {
        int workIndex = message.indexOf("업무");
        if (workIndex < 0) return null;
        int start = -1;
        for (String marker : List.of("프로젝트에서", "프로젝트에", "프로젝트의")) {
            int index = message.lastIndexOf(marker, workIndex);
            if (index >= 0) start = Math.max(start, index + marker.length());
        }
        String value = message.substring(start < 0 ? 0 : start, workIndex)
                .replaceAll("^(?:새로운|새|특정)\\s*", "")
                .replaceAll(".*프로젝트로\\s*", "")
                .trim();
        return value.isBlank() || List.of("주요", "등록된", "어떤", "진행한").contains(value)
                ? null
                : value;
    }

    private List<LocalDate> parseDates(String message) {
        List<LocalDate> dates = new ArrayList<>();
        for (DateInput input : findDates(message)) dates.add(input.toDate(LocalDate.now().getYear()));
        return dates;
    }

    private List<DateInput> findDates(String message) {
        List<DateInput> dates = new ArrayList<>();
        Matcher matcher = DATE_PATTERN.matcher(message);
        while (matcher.find()) {
            String year = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            dates.add(new DateInput(year == null ? null : Integer.parseInt(year),
                    Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4))));
        }
        return dates;
    }

    private LocalDate parseWorkDate(String message) {
        AiDateExpressionService.DateSelection selection = dateExpressionService.resolve(message);
        if (selection != null && selection.isSingleDay()) return selection.start();
        List<LocalDate> dates = parseDates(message);
        return dates.isEmpty() ? null : dates.get(0);
    }

    private String parseEnvironment(String message) {
        if (message.contains("파견")) return "dispatch";
        if (message.contains("내근")) return "office";
        return null;
    }

    private String parseUpdatedEnvironment(String message) {
        Matcher field = ENVIRONMENT_FIELD.matcher(message);
        if (!field.find()) return null;
        Matcher values = ENVIRONMENT_VALUE.matcher(message.substring(field.end()));
        String target = null;
        while (values.find()) target = values.group();
        return target == null ? null : "내근".equals(target) ? "office" : "dispatch";
    }

    private String parseStatus(String message) {
        if (message.contains("진행중") || message.contains("진행 중")) return "in_progress";
        if (message.contains("완료") || message.replace("종료일", "").contains("종료")) return "completed";
        if (message.contains("보류")) return "on_hold";
        if (message.contains("예정")) return "planned";
        return null;
    }

    private String findGroup(Pattern pattern, String message) {
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^가-힣a-z0-9]", "");
    }

    private String environmentLabel(String value) {
        return "dispatch".equals(value) ? "파견" : "내근";
    }

    private String statusLabel(String value) {
        return switch (value) {
            case "in_progress" -> "진행중";
            case "completed" -> "완료";
            case "on_hold" -> "보류";
            default -> "예정";
        };
    }

    private record ProjectSelection(ProjectList project, String message) {
        boolean resolved() {
            return project != null;
        }
    }

    private record PendingProjectChoice(ProjectSelection selection, String request) {
    }

    private record DateInput(Integer year, int month, int day) {
        LocalDate toDate(int defaultYear) {
            try {
                return LocalDate.of(year == null ? defaultYear : year, month, day);
            } catch (DateTimeException exception) {
                throw new IllegalArgumentException("올바른 날짜를 입력해 주세요.");
            }
        }
    }

    private record WorkSelection(WorkList work, String message) {
        boolean resolved() {
            return work != null;
        }
    }
}
