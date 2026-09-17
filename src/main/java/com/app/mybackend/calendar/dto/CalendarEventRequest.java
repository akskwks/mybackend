package com.app.mybackend.calendar.dto;

import java.time.LocalDate;
import java.time.LocalTime;

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
