package com.app.mybackend.work.repository;

import com.app.mybackend.work.entity.ProjectList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
*
* 클래스명 : ProjectListRepository
* 역할 : 프로젝트 데이터를 조회하고 저장하는 JPA 저장소
* 생성일 : 2026-09-18
*
**/
public interface ProjectListRepository extends JpaRepository<ProjectList, Long> {
    List<ProjectList> findByUserNumOrderByUpdatedAtDesc(Long userNum);
    Optional<ProjectList> findByProjectIdAndUserNum(Long projectId, Long userNum);
}
