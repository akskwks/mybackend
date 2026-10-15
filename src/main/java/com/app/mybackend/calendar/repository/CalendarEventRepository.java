package com.app.mybackend.calendar.repository;

import com.app.mybackend.calendar.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByUserNumOrderByEventDateAscStartTimeAsc(Long userNum);
    List<CalendarEvent> findByUserNumAndEventDateBetweenOrderByEventDateAscStartTimeAsc(
            Long userNum, LocalDate start, LocalDate end);
    Optional<CalendarEvent> findByEventIdAndUserNum(Long eventId, Long userNum);
}
