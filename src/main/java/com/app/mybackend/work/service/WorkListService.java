package com.app.mybackend.work.service;

import com.app.mybackend.work.dto.WorkListRequest;
import com.app.mybackend.work.entity.WorkList;
import com.app.mybackend.work.repository.WorkListRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class WorkListService {

    private static final Set<String> STATUSES =
        Set.of("planned", "in_progress", "completed", "on_hold");

    private final WorkListRepository repository;

    public WorkListService(WorkListRepository workListRepository) {
        this.repository = workListRepository;
    }

    public List<WorkList> findAll(Long projectId, LocalDate date) {
        if (date != null) {
            return repository.findByProjectIdAndWorkDateOrderByUpdatedAt(projectId, date);
        }
        return repository.findByProjectIdOrderByWorkDateDescUpdatedAtDesc(projectId);
    }

    public WorkList findById(Long workId) {
        return repository.findById(workId)
                .orElseThrow(() -> new RuntimeException("업무를 찾을 수 없습니다."));
    }

//    public List<WorkList> findBetween(LocalDate start, LocalDate end) {
//        return repository.findByWorkDateBetweenOrderByWorkDateDescUpdatedAtDesc(start, end);
//    }

    public WorkList create(WorkListRequest request) {
        WorkList workList = new WorkList();
        apply(workList, request);
        return repository.save(workList);
    }

    public WorkList update(Long workId, WorkListRequest request) {
        WorkList workList = findById(workId);
        apply(workList, request);
        return repository.save(workList);
    }

    public void delete(Long workId) {
        repository.deleteById(workId);
    }

    private void apply(WorkList workList, WorkListRequest request) {
        validate(request);

        workList.setProjectId(request.projectId());
        workList.setWorkDate(request.workDate());
        workList.setWorkTitle(request.workTitle());
        workList.setWorkCnnt(request.workCnnt());
        workList.setWorkStatus(request.workStatus());
        workList.setWorkProgress(request.workProgress());
    }

    private void validate(WorkListRequest request) {
        if (request.projectId() == null) {
            throw new IllegalArgumentException("프로젝트는 필수입니다.");
        }

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
