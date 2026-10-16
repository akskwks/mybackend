package com.app.mybackend.calendar.service;

import com.app.mybackend.calendar.dto.CalendarEventRequest;
import com.app.mybackend.calendar.entity.CalendarEvent;
import com.app.mybackend.calendar.repository.CalendarEventRepository;
import com.app.mybackend.config.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
*
* 클래스명 : CalendarEventService
* 역할 : 사용자별 일정 조회와 일정 등록, 수정, 삭제를 처리하는 서비스
* 생성일 : 2026-09-17
*
**/
@Service
public class CalendarEventService {
    private final CalendarEventRepository repository;
    private final CurrentUser currentUser;

    public CalendarEventService(CalendarEventRepository calendarEventRepository, CurrentUser currentUser) {
        this.repository = calendarEventRepository;
        this.currentUser = currentUser;
    }

    public List<CalendarEvent> findAll() {
        return repository.findByUserNumOrderByEventDateAscStartTimeAsc(currentUser.number());
    }

    public List<CalendarEvent> findBetween(LocalDate start, LocalDate end) {
        return repository.findByUserNumAndEventDateBetweenOrderByEventDateAscStartTimeAsc(
                currentUser.number(), start, end);
    }

    public CalendarEvent create(CalendarEventRequest request) {
        CalendarEvent event = new CalendarEvent();
        event.setUserNum(currentUser.number());
        apply(event, request);
        return repository.save(event);
    }

    public CalendarEvent update(Long eventId, CalendarEventRequest request) {
        CalendarEvent event = requireEvent(eventId);
        apply(event, request);
        return repository.save(event);
    }

    public void delete(Long eventId) {
        repository.delete(requireEvent(eventId));
    }

    public CalendarEvent findById(Long eventId) {
        return requireEvent(eventId);
    }

    private CalendarEvent requireEvent(Long eventId) {
        return repository.findByEventIdAndUserNum(eventId, currentUser.number())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "일정을 찾을 수 없습니다."));
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
