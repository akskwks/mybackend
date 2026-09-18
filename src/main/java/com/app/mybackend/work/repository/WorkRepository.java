package com.app.mybackend.work.repository;

import com.app.mybackend.work.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WorkRepository extends JpaRepository<Work, Long> {
    List<Work> findAllByOrderByWorkDateDescUpdatedAtDesc();

    List<Work> findByWorkDateOrderByUpdatedAtDesc(LocalDate workDate);

    List<Work> findByWorkDateBetweenOrderByWorkDateDescUpdatedAtDesc(LocalDate start, LocalDate end);
}


