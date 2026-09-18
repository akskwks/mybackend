package com.app.mybackend.work.repository;

import com.app.mybackend.work.entity.WorkList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WorkListRepository extends JpaRepository<WorkList, Long> {
    List<WorkList> findByProjectIdOrderByWorkDateDescUpdatedAtDesc(Long projectId);

    List<WorkList> findByProjectIdAndWorkDateOrderByUpdatedAt(Long projectId, LocalDate workDate);

    boolean existsByProjectId(Long projectId);
}


