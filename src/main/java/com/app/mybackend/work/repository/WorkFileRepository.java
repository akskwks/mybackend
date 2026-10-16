package com.app.mybackend.work.repository;

import com.app.mybackend.work.entity.WorkFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
*
* 클래스명 : WorkFileRepository
* 역할 : 업무 첨부파일 메타데이터를 조회하고 저장하는 JPA 저장소
* 생성일 : 2026-09-21
*
**/
public interface WorkFileRepository extends JpaRepository<WorkFile, Long> {
    List<WorkFile> findByWorkIdOrderByCreatedAtAsc(Long id);
    List<WorkFile> findByWorkIdAndWorkFileIdIn(Long id, Collection<Long> workFields);
}
