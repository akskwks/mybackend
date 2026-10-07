package com.app.mybackend.aichat.service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiDateExpressionService {
    private static final Pattern ISO_DATE = Pattern.compile("20\\d{2}[-./]\\d{1,2}[-./]\\d{1,2}");
    private static final Pattern MONTH_DAY = Pattern.compile("(?:(20\\d{2})년\\s*)?(\\d{1,2})월\\s*(\\d{1,2})일");
    private static final Pattern RELATIVE = Pattern.compile("(\\d+|한|두|세)\\s*(일|주|개월|달)\\s*(뒤|후|전)(?:에)?");
    private static final Pattern WEEK = Pattern.compile("일주일\\s*(뒤|후|전)(?:에)?");
    private static final Pattern WEEKEND = Pattern.compile("(지난|이번|다음)\\s*주말");
    private static final Pattern NAMED_WEEK = Pattern.compile("(지난|이번|다음)\\s*주");
    private static final Pattern NAMED_MONTH = Pattern.compile("(지난|이번|다음)\\s*달");

    public DateSelection resolve(String message) {
        return resolve(message, LocalDate.now());
    }

    public DateSelection resolve(String message, LocalDate today) {
        if (message == null || message.isBlank()) return null;

        Matcher match = ISO_DATE.matcher(message);
        if (match.find()) {
            String[] parts = match.group().split("[-./]");
            return day(LocalDate.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])), match.group());
        }
        match = MONTH_DAY.matcher(message);
        if (match.find()) {
            int year = match.group(1) == null ? today.getYear() : Integer.parseInt(match.group(1));
            return day(LocalDate.of(year, Integer.parseInt(match.group(2)), Integer.parseInt(match.group(3))), match.group());
        }

        match = RELATIVE.matcher(message);
        if (match.find()) {
            int amount = switch (match.group(1)) {
                case "한" -> 1;
                case "두" -> 2;
                case "세" -> 3;
                default -> Integer.parseInt(match.group(1));
            };
            if ("전".equals(match.group(3))) amount = -amount;
            LocalDate date = switch (match.group(2)) {
                case "일" -> today.plusDays(amount);
                case "주" -> today.plusWeeks(amount);
                default -> today.plusMonths(amount);
            };
            return day(date, match.group());
        }
        match = WEEK.matcher(message);
        if (match.find()) {
            return day(today.plusWeeks("전".equals(match.group(1)) ? -1 : 1), match.group());
        }

        match = WEEKEND.matcher(message);
        if (match.find()) {
            LocalDate saturday = today.with(DayOfWeek.MONDAY).plusDays(5)
                    .plusWeeks(namedOffset(match.group(1)));
            return new DateSelection(saturday, saturday.plusDays(1), match.group());
        }
        match = NAMED_WEEK.matcher(message);
        if (match.find()) {
            LocalDate monday = today.with(DayOfWeek.MONDAY).plusWeeks(namedOffset(match.group(1)));
            return new DateSelection(monday, monday.plusDays(6), match.group());
        }
        match = NAMED_MONTH.matcher(message);
        if (match.find()) {
            YearMonth month = YearMonth.from(today).plusMonths(namedOffset(match.group(1)));
            return new DateSelection(month.atDay(1), month.atEndOfMonth(), match.group());
        }

        for (String expression : new String[]{"그저께", "어제", "모레", "내일", "오늘"}) {
            if (!message.contains(expression)) continue;
            int offset = switch (expression) {
                case "그저께" -> -2;
                case "어제" -> -1;
                case "모레" -> 2;
                case "내일" -> 1;
                default -> 0;
            };
            return day(today.plusDays(offset), expression);
        }
        return null;
    }

    public String withoutDateExpression(String message) {
        DateSelection selection = resolve(message);
        return selection == null ? message : message.replace(selection.expression(), "");
    }

    private int namedOffset(String word) {
        return switch (word) {
            case "지난" -> -1;
            case "다음" -> 1;
            default -> 0;
        };
    }

    private DateSelection day(LocalDate date, String expression) {
        return new DateSelection(date, date, expression);
    }

    public record DateSelection(LocalDate start, LocalDate end, String expression) {
        public boolean isSingleDay() {
            return start.equals(end);
        }
    }
}
