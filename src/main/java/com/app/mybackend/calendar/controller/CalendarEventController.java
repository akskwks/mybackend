package com.app.mybackend.calendar.controller;

import com.app.mybackend.calendar.dto.CalendarEventRequest;
import com.app.mybackend.calendar.entity.CalendarEvent;
import com.app.mybackend.calendar.service.CalendarEventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
*
* 클래스명 : CalendarEventController
* 역할 : 캘린더 일정 목록 및 등록, 수정, 삭제 API를 제공하는 컨트롤러
* 생성일 : 2026-09-17
*
**/
@RestController
@RequestMapping("/api/calendar/events")
public class CalendarEventController {
    private final CalendarEventService service;

    public CalendarEventController(CalendarEventService service) {
        this.service = service;
    }

    @GetMapping
    public List<CalendarEvent> findAll() {
        return service.findAll();
    }

    @PostMapping
    public CalendarEvent create(@RequestBody CalendarEventRequest request) {
        return service.create(request);
    }

    @PutMapping("/{eventId}")
    public CalendarEvent update(@PathVariable Long eventId, @RequestBody CalendarEventRequest request) {
        return service.update(eventId, request);
    }

    @DeleteMapping("/{eventId}")
    public void delete(@PathVariable Long eventId) {
        service.delete(eventId);
    }
}
