package com.app.mybackend.calendar.repository;

import com.app.mybackend.calendar.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findAllByOrderByEventDateAscStartTimeAsc();
    List<CalendarEvent> findByEventDateBetweenOrderByEventDateAscStartTimeAsc(LocalDate start, LocalDate end);
}
