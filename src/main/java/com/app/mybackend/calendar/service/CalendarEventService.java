package com.app.mybackend.calendar.service;

import com.app.mybackend.calendar.dto.CalendarEventRequest;
import com.app.mybackend.calendar.entity.CalendarEvent;
import com.app.mybackend.calendar.repository.CalendarEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarEventService {
    private final CalendarEventRepository repository;

    public CalendarEventService(CalendarEventRepository calendarEventRepository) {
        this.repository = calendarEventRepository;
    }

    public List<CalendarEvent> findAll() {
        return repository.findAllByOrderByEventDateAscStartTimeAsc();
    }

    public CalendarEvent create(CalendarEventRequest request) {
        CalendarEvent event = new CalendarEvent();
        apply(event, request);
        return repository.save(event);
    }

    public CalendarEvent update(Long eventId, CalendarEventRequest request) {
        CalendarEvent event = repository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));
        apply(event, request);
        return repository.save(event);
    }

    public void delete(Long eventId) {
        repository.deleteById(eventId);
    }


    private void apply(CalendarEvent event, CalendarEventRequest request) {
        event.setEventTitle(request.eventTitle());
        event.setEventDate(request.eventDate());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setEventCatg(request.eventCatg());
        event.setColor(request.color());
        event.setEvent_dsc(request.event_dsc());
    }
}
