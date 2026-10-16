package com.app.mybackend.work.repository;

import com.app.mybackend.work.entity.WorkList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
*
* 클래스명 : WorkListRepository
* 역할 : 프로젝트별 업무 데이터를 조회하고 저장하는 JPA 저장소
* 생성일 : 2026-09-18
*
**/
public interface WorkListRepository extends JpaRepository<WorkList, Long> {
    List<WorkList> findByProjectIdOrderByWorkDateDescUpdatedAtDesc(Long projectId);

    List<WorkList> findByProjectIdAndWorkDateOrderByUpdatedAt(Long projectId, LocalDate workDate);

    boolean existsByProjectId(Long projectId);

    long countByProjectId(Long projectId);
}


