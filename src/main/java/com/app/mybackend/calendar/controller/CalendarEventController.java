package com.app.mybackend.calendar.controller;

import com.app.mybackend.calendar.dto.CalendarEventRequest;
import com.app.mybackend.calendar.entity.CalendarEvent;
import com.app.mybackend.calendar.service.CalendarEventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
