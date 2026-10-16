package com.app.mybackend.work.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
*
* 클래스명 : WorkFile
* 역할 : 업무에 첨부된 파일의 메타데이터를 나타내는 JPA 엔티티
* 생성일 : 2026-09-21
*
**/
@Entity
@Getter
@Setter
@Table(name = "app_work_file")
public class WorkFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_file_id", nullable = false)
    private Long workFileId;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "orgn_file_name", length = 255, nullable = false)
    private String orgnFileName;

    @Column(name = "sv_file_name", length = 100, nullable = false)
    private String svFileName;

    @Column(name = "file_path", length = 500, nullable = false)
    private String filePath;

    @Column(name = "file_extension", length = 30, nullable = false)
    private String fileExtension;

    @Column(name = "mime_type", length = 150, nullable = false)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
