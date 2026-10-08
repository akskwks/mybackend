package com.app.mybackend.aichat.service;

import com.app.mybackend.aichat.entity.AiMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class AiIntentService {

    public Intent detect(String message) {
        String text = message.toLowerCase(Locale.ROOT);
        boolean calendar = containsAny(text, "일정", "스케줄", "캘린더");
        boolean memo = containsAny(text, "메모", "todo", "할 일");
        boolean project = text.contains("프로젝트");
        boolean work = text.contains("업무");
        boolean create = text.matches("(?s).*(?:추가|등록(?!된|되|돼)|생성|만들어).*");
        boolean update = containsAny(text, "수정", "변경", "바꿔", "고쳐");
        boolean delete = containsAny(text, "삭제", "지워", "제거");
        boolean workIsTarget = work && (!project || text.lastIndexOf("업무") > text.lastIndexOf("프로젝트"));

        if (workIsTarget && create) return Intent.WORK_CREATE;
        if (workIsTarget && update) return Intent.WORK_UPDATE;
        if (workIsTarget && delete) return Intent.WORK_DELETE;
        if (project && create) return Intent.PROJECT_CREATE;
        if (project && (update || hasProjectChange(text))) return Intent.PROJECT_UPDATE;
        if (project && delete) return Intent.PROJECT_DELETE;
        if (work && create) return Intent.WORK_CREATE;
        if (work && update) return Intent.WORK_UPDATE;
        if (work && delete) return Intent.WORK_DELETE;
        if (!project && !work && hasProjectChange(text)) return Intent.PROJECT_UPDATE;
        if (calendar && containsAny(text, "추가", "등록", "잡아", "만들어")) {
            return Intent.CALENDAR_CREATE;
        }
        if (calendar) {
            return Intent.CALENDAR_QUERY;
        }
        if (memo && containsAny(text, "요약", "정리", "중요한 내용")) {
            return Intent.MEMO_SUMMARY;
        }
        if (memo) {
            return Intent.MEMO_SEARCH;
        }
        if (project && containsAny(text, "기간", "언제부터", "언제까지")) {
            return Intent.PROJECT_PERIOD_QUERY;
        }
        if (work) {
            return Intent.PROJECT_WORK_QUERY;
        }
        if (project && (containsAny(text, "목록", "등록된 프로젝트", "프로젝트들", "뭐가 있어")
                || isProjectListPhrase(text))) {
            return Intent.PROJECT_LIST_QUERY;
        }
        if (project) {
            return Intent.PROJECT_DETAIL_QUERY;
        }
        return Intent.GENERAL_CHAT;
    }

    public Intent detect(String message, List<AiMessage> history) {
        Intent detected = detect(message);
        if (detected != Intent.GENERAL_CHAT || history == null || history.isEmpty()) {
            return detected;
        }

        for (int index = history.size() - 1; index >= 0; index--) {
            AiMessage previous = history.get(index);
            if ("exception".equals(previous.getRole())) return detected;
            if (!"assistant".equals(previous.getRole())) continue;
            if (previous.getContent().contains("프로젝트 등록에 필요한 정보를")) {
                return Intent.PROJECT_CREATE;
            }
            if (previous.getContent().contains("업무 등록에 필요한 정보를")) {
                return Intent.WORK_CREATE;
            }
            if (previous.getContent().contains("프로젝트 수정 대상을")) {
                return Intent.PROJECT_UPDATE;
            }
            if (previous.getContent().contains("프로젝트 수정에 필요한 정보를")) {
                return Intent.PROJECT_UPDATE;
            }
            if (previous.getContent().contains("프로젝트 삭제 대상을")) {
                return Intent.PROJECT_DELETE;
            }
            if (previous.getContent().contains("업무 수정 대상을")) {
                return Intent.WORK_UPDATE;
            }
            if (previous.getContent().contains("업무 삭제 대상을")) {
                return Intent.WORK_DELETE;
            }
            break;
        }
        return detected;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    private boolean hasProjectChange(String text) {
        if (text.matches("(?s).*\\S+\\s+(?:상태|진행사항|분류|근무\\s*환경|시작일|종료일)(?:을|를|은|는)?\\s+\\S+.*")) {
            return true;
        }
        return text.matches("(?s).*\\S+\\s+일정(?:을|은|는)?\\s+.*\\d{1,2}[-/월]\\s*\\d{1,2}.*");
    }

    private boolean isProjectListPhrase(String text) {
        String scope = text.replaceAll("(지난|이번|다음)\\s*(주|달)", "")
                .replaceAll("[?!.\\s]", "")
                .trim();
        return "프로젝트".equals(scope) || "현재프로젝트".equals(scope);
    }

    public enum Intent {
        GENERAL_CHAT,
        CALENDAR_QUERY,
        CALENDAR_CREATE,
        MEMO_SEARCH,
        MEMO_SUMMARY,
        PROJECT_LIST_QUERY,
        PROJECT_DETAIL_QUERY,
        PROJECT_PERIOD_QUERY,
        PROJECT_WORK_QUERY,
        PROJECT_CREATE,
        PROJECT_UPDATE,
        PROJECT_DELETE,
        WORK_CREATE,
        WORK_UPDATE,
        WORK_DELETE
    }
}
