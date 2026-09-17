package com.app.mybackend.aichat.service;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AiIntentService {

    public Intent detect(String message) {
        String text = message.toLowerCase(Locale.ROOT);
        boolean calendar = containsAny(text, "일정", "스케줄", "캘린더");
        boolean memo = containsAny(text, "메모", "todo", "할 일");

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
        MEMO_SUMMARY
    }
}
