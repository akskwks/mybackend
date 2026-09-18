package com.app.mybackend.work.service;

import com.app.mybackend.work.dto.WorkRequest;
import com.app.mybackend.work.entity.Work;
import com.app.mybackend.work.repository.WorkRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class WorkService {

    private static final Set<String> STATUSES =
        Set.of("planned", "in_progress", "completed", "on_hold");

    private final WorkRepository repository;

    public WorkService(WorkRepository workRepository) {
        this.repository = workRepository;
    }

    public List<Work> findAll(LocalDate date) {
        if (date != null) {
            return repository.findByWorkDateOrderByUpdatedAtDesc(date);
        }
        return repository.findAllByOrderByWorkDateDescUpdatedAtDesc();
    }

    public Work findById(Long workId) {
        return repository.findById(workId)
                .orElseThrow(() -> new RuntimeException("업무를 찾을 수 없습니다."));
    }

    public List<Work> findBetween(LocalDate start, LocalDate end) {
        return repository.findByWorkDateBetweenOrderByWorkDateDescUpdatedAtDesc(start, end);
    }

    public Work create(WorkRequest request) {
        Work work = new Work();
        apply(work, request);
        return repository.save(work);
    }

    public Work update(Long workId, WorkRequest request) {
        Work work = findById(workId);
        apply(work, request);
        return repository.save(work);
    }

    public void delete(Long workId) {
        repository.deleteById(workId);
    }

    private void apply(Work work, WorkRequest request) {
        validate(request);

        work.setWorkDate(request.workDate());
        work.setWorkTitle(request.workTitle());
        work.setWorkCnnt(request.workCnnt());
        work.setWorkStatus(request.workStatus());
        work.setWorkProgress(request.workProgress());
    }

    private void validate(WorkRequest request) {
        if (request.workDate() == null) {
            throw new IllegalArgumentException("업무일자는 필수입니다.");
        }

        if (request.workTitle() == null || request.workTitle().isBlank()) {
            throw new IllegalArgumentException("업무 제목은 필수입니다.");
        }

        if (!STATUSES.contains(request.workStatus())) {
            throw new IllegalArgumentException("올바르지 않은 업무 상태입니다.");
        }

        if (request.workProgress() == null
                || request.workProgress() < 0
                || request.workProgress() > 100) {
            throw new IllegalArgumentException("진행률은 0부터 100 사이여야 합니다.");
        }
    }


}
