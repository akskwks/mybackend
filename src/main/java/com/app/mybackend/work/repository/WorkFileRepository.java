package com.app.mybackend.work.repository;

import com.app.mybackend.work.entity.WorkFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface WorkFileRepository extends JpaRepository<WorkFile, Long> {
    List<WorkFile> findByWorkIdOrderByCreatedAtAsc(Long id);
    List<WorkFile> findByWorkIdAndWorkFileIdIn(Long id, Collection<Long> workFields);
}
