package com.app.mybackend.work.repository;

import com.app.mybackend.work.entity.ProjectList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectListRepository extends JpaRepository<ProjectList, Long> {
    List<ProjectList> findAllByOrderByUpdatedAtDesc();
}
