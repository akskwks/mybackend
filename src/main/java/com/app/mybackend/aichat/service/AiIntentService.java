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
        boolean create = containsAny(text, "추가해", "추가 해", "등록해", "등록 해", "생성해", "생성 해", "만들어");
        boolean update = containsAny(text, "수정", "변경", "바꿔", "고쳐");
        boolean delete = containsAny(text, "삭제", "지워", "제거");
        boolean workIsTarget = work && (!project || text.lastIndexOf("업무") > text.lastIndexOf("프로젝트"));

        if (workIsTarget && create) return Intent.WORK_CREATE;
        if (workIsTarget && update) return Intent.WORK_UPDATE;
        if (workIsTarget && delete) return Intent.WORK_DELETE;
        if (project && create) return Intent.PROJECT_CREATE;
        if (project && update) return Intent.PROJECT_UPDATE;
        if (project && delete) return Intent.PROJECT_DELETE;
        if (work && create) return Intent.WORK_CREATE;
        if (work && update) return Intent.WORK_UPDATE;
        if (work && delete) return Intent.WORK_DELETE;
        if (project && containsAny(text, "기간", "언제부터", "언제까지")) {
            return Intent.PROJECT_PERIOD_QUERY;
        }
        if (work && containsAny(text, "주요", "목록", "알려", "조회", "뭐", "어떤", "보여")) {
            return Intent.PROJECT_WORK_QUERY;
        }
        if (project && containsAny(text, "목록", "등록된 프로젝트", "프로젝트들", "뭐가 있어")) {
            return Intent.PROJECT_LIST_QUERY;
        }
        if (project && containsAny(text, "정보", "대해", "알려", "조회", "보여")) {
            return Intent.PROJECT_DETAIL_QUERY;
        }

        if (calendar && containsAny(text, "추가", "등록", "잡아", "만들어")) {
            return Intent.CALENDAR_CREATE;
        }
        if (calendar && containsAny(text, "알려", "조회", "있어", "정리", "보여")) {
            return Intent.CALENDAR_QUERY;
        }
        if (memo && containsAny(text, "요약", "정리", "중요한 내용")) {
            return Intent.MEMO_SUMMARY;
        }
        if (memo && containsAny(text, "찾아", "검색", "있어", "보여")) {
            return Intent.MEMO_SEARCH;
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
