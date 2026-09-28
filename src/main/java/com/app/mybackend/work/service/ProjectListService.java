package com.app.mybackend.work.service;

import com.app.mybackend.work.dto.ProjectListRequest;
import com.app.mybackend.work.entity.ProjectList;
import com.app.mybackend.work.repository.ProjectListRepository;
import com.app.mybackend.work.repository.WorkListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectListService {

    private static final Set<String> ENVIRONMENTS =
            Set.of("office", "dispatch");

    private static final Set<String> STATUSES =
            Set.of("planned", "in_progress", "completed", "on_hold");

    private final ProjectListRepository projectListRepository;
    private final WorkListRepository workListRepository;

    public List<ProjectList> findAll() {
        return projectListRepository.findAllByOrderByUpdatedAtDesc();
    }

    public ProjectList findById(Long id) {
        return projectListRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "프로젝트를 찾을 수 없습니다."
                ));
    }

    public ProjectList create(ProjectListRequest request) {
        ProjectList projectList = new ProjectList();
        apply(projectList, request);
        return projectListRepository.save(projectList);
    }

    public ProjectList update(Long projectId, ProjectListRequest request) {
        ProjectList projectList = findById(projectId);
        apply(projectList, request);
        return projectListRepository.save(projectList);
    }

    public void delete(Long projectId) {
        if (workListRepository.existsByProjectId(projectId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "연결된 업무가 있는 프로젝트는 삭제할 수 없습니다."
            );
        }
        projectListRepository.deleteById(projectId);
    }


    private void apply(
            ProjectList projectList,
            ProjectListRequest projectListRequest
    ) {
        validate(projectListRequest);

        projectList.setWorkEnvironment(projectListRequest.workEnvironment());
        projectList.setProjectName(projectListRequest.projectName());
        projectList.setProjectStatus(projectListRequest.projectStatus());
        projectList.setStartDate(projectListRequest.startDate());
        projectList.setEndDate(projectListRequest.endDate());
    }


    private void validate(ProjectListRequest request) {
        if (!ENVIRONMENTS.contains(request.workEnvironment())) {
            throw new IllegalArgumentException("올바르지 않은 근무 환경입니다.");
        }

        if (!STATUSES.contains(request.projectStatus())) {
            throw new IllegalArgumentException("올바르지 않은 진행 상태입니다.");
        }

        if (request.projectName() == null || request.projectName().isEmpty()) {
            throw new IllegalArgumentException("프로젝트명은 필수입니다.");
        }

        if (request.startDate() == null || request.endDate() == null) {
            throw new IllegalArgumentException("프로젝트 시작일과 종료일은 필수입니다.");
        }

        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }
    }
}
