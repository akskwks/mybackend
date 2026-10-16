package com.app.mybackend.calendar.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
*
* 클래스명 : CalendarEvent
* 역할 : 일정 날짜와 시간, 제목, 분류를 나타내는 JPA 엔티티
* 생성일 : 2026-09-17
*
**/
@Entity
@Getter
@Setter
@Table(name = "app_calendar")
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "user_num", nullable = false)
    private Long userNum;

    @Column(name = "event_title")
    private String eventTitle;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "event_catg")
    private String eventCatg;

    @Column(name = "color")
    private String color;

    @Column(name = "event_dsc", length = 2000)
    private String event_dsc;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
