package com.app.mybackend.calendar.repository;

import com.app.mybackend.calendar.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
*
* 클래스명 : CalendarEventRepository
* 역할 : 사용자별 일정 데이터를 조회하고 저장하는 JPA 저장소
* 생성일 : 2026-09-17
*
**/
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByUserNumOrderByEventDateAscStartTimeAsc(Long userNum);
    List<CalendarEvent> findByUserNumAndEventDateBetweenOrderByEventDateAscStartTimeAsc(
            Long userNum, LocalDate start, LocalDate end);
    Optional<CalendarEvent> findByEventIdAndUserNum(Long eventId, Long userNum);
}
