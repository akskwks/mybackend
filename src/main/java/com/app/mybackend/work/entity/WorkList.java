package com.app.mybackend.work.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "app_work")
public class WorkList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "work_title", length = 100, nullable = false)
    private String workTitle;

    @Column(name = "work_cnnt", length = 10000)
    private String workCnnt;

    @Column(name = "work_status", length = 20, nullable = false)
    private String workStatus;

    @Column(name = "work_progress", nullable = false)
    private int workProgress;

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
