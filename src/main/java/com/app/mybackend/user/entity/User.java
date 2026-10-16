package com.app.mybackend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
*
* 클래스명 : User
* 역할 : 로그인 정보와 권한을 보관하는 사용자 JPA 엔티티
* 생성일 : 2026-10-14
*
**/
@Entity
@Getter
@Setter
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_num")
    private Long userNum;

    @Column(name = "user_id", length = 100, nullable = false, unique = true)
    private String userId;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "user_name", length = 100, nullable = false)
    private String userName;

    @Column(name = "role", length = 20, nullable = false)
    private String role;

    @Column(name = "use_yn", length = 1, nullable = false)
    private String useYn;

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
