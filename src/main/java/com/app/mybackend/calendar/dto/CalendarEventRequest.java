package com.app.mybackend.calendar.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
*
* 클래스명 : CalendarEventRequest
* 역할 : 일정 등록 및 수정에 필요한 입력 데이터를 전달하는 요청 DTO
* 생성일 : 2026-09-17
*
**/
public record CalendarEventRequest(
        String eventTitle,
        LocalDate eventDate,
        LocalTime startTime,
        LocalTime endTime,
        String eventCatg,
        String color,
        String event_dsc
) {
}
