package com.app.mybackend.memo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
*
* 클래스명 : Memo
* 역할 : 사용자 메모의 제목, 분류, 내용, 수정일을 나타내는 JPA 엔티티
* 생성일 : 2026-09-17
*
**/
@Entity
@Getter
@Setter
@Table(name = "app_memo")
public class Memo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memo_id")
    private Long memoId;

    @Column(name = "user_num", nullable = false)
    private Long userNum;

    @Column(name = "memo_title", length = 40, nullable = false)
    private String memoTitle;

    @Column(name = "memo_sort", nullable = false)
    private String memoSort;

    @Column(name = "memo_cnnt", length = 10000)
    private String memoCnnt;

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
