package com.app.mybackend.work.service;

import com.app.mybackend.work.dto.WorkListRequest;
import com.app.mybackend.work.entity.WorkList;
import com.app.mybackend.work.repository.WorkListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WorkListService {

    private static final Set<String> STATUSES =
        Set.of("planned", "in_progress", "completed", "on_hold");

    private final WorkListRepository repository;
    private final WorkFileService workFileService;

    @Transactional(readOnly = true)
    public List<WorkList> findAll(Long projectId, LocalDate date) {
        if (date != null) {
            return repository.findByProjectIdAndWorkDateOrderByUpdatedAt(projectId, date);
        }
        return repository.findByProjectIdOrderByWorkDateDescUpdatedAtDesc(projectId);
    }

    @Transactional(readOnly = true)
    public WorkList findById(Long workId) {
        return repository.findById(workId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "업무를 찾을 수 없습니다."));
    }

//    public List<WorkList> findBetween(LocalDate start, LocalDate end) {
//        return repository.findByWorkDateBetweenOrderByWorkDateDescUpdatedAtDesc(start, end);
//    }

    @Transactional
    public WorkList create(WorkListRequest request) {
        WorkList workList = new WorkList();
        apply(workList, request);
        return repository.save(workList);
    }

    @Transactional
    public WorkList createWithFiles(WorkListRequest request, List<MultipartFile> files) {
        WorkList workList = create(request);
        repository.flush();
        workFileService.saveAll(workList.getWorkId(), files);
        return workList;
    }

    @Transactional
    public WorkList update(Long workId, WorkListRequest request) {
        WorkList workList = findById(workId);
        apply(workList, request);
        return repository.save(workList);
    }

    @Transactional
    public WorkList updateWithFiles(
            Long workId,
            WorkListRequest request,
            List<MultipartFile> files,
            List<Long> deletedFileIds
    ) {
        WorkList workList = update(workId, request);
        workFileService.saveAll(workId, files);
        workFileService.deleteSelected(workId, deletedFileIds);
        return workList;
    }

    @Transactional
    public void delete(Long workId) {
        WorkList workList = findById(workId);
        workFileService.deleteAllForWork(workId);
        repository.delete(workList);
    }

    private void apply(WorkList workList, WorkListRequest request) {
        validate(request);

        workList.setProjectId(request.projectId());
        workList.setWorkDate(request.workDate());
        workList.setWorkTitle(request.workTitle());
        workList.setWorkCnnt(request.workCnnt());
        workList.setWorkStatus(request.workStatus());
    }

    private void validate(WorkListRequest request) {
        if (request.projectId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "프로젝트는 필수입니다.");
        }
        if (request.workDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업무일자는 필수입니다.");
        }
        if (request.workTitle() == null || request.workTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업무 제목은 필수입니다.");
        }
        if (!STATUSES.contains(request.workStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 업무 상태입니다.");
        }
    }
}
