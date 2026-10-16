package com.app.mybackend.user.repository;

import com.app.mybackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
*
* 클래스명 : UserRepository
* 역할 : 사용자 식별 정보와 계정을 조회하는 JPA 저장소
* 생성일 : 2026-10-14
*
**/
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    boolean existsByUserId(String userId);
}
