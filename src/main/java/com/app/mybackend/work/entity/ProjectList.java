package com.app.mybackend.work.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
*
* 클래스명 : ProjectList
* 역할 : 근무 환경, 기간, 진행 상태를 포함한 프로젝트 JPA 엔티티
* 생성일 : 2026-09-18
*
**/
@Entity
@Getter
@Setter
@Table(name = "app_project")
public class ProjectList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "user_num", nullable = false)
    private Long userNum;

    @Column(name = "work_environment", length = 20, nullable = false)
    private String workEnvironment;

    @Column(name = "project_name", length = 100, nullable = false)
    private String projectName;

    @Column(name = "project_status", length = 20, nullable = false)
    private String projectStatus;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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
