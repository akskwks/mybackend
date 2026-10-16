package com.app.mybackend.aichat.service;

import com.app.mybackend.work.dto.ProjectListRequest;
import com.app.mybackend.work.dto.WorkListRequest;
import com.app.mybackend.work.entity.ProjectList;
import com.app.mybackend.work.entity.WorkList;
import com.app.mybackend.work.repository.WorkListRepository;
import com.app.mybackend.work.service.ProjectListService;
import com.app.mybackend.work.service.WorkListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
*
* 클래스명 : ProjectWorkAiTool
* 역할 : AI가 프로젝트와 업무 기능을 서비스 계층을 통해 호출하도록 연결하는 도구
* 생성일 : 2026-09-28
*
**/
@Service
@RequiredArgsConstructor
public class ProjectWorkAiTool {

    private final ProjectListService projectService;
    private final WorkListService workService;
    private final WorkListRepository workRepository;

    public List<ProjectList> findProjects() {
        return projectService.findAll();
    }

    public long countWorks(Long projectId) {
        projectService.findById(projectId);
        return workRepository.countByProjectId(projectId);
    }

    public List<WorkList> findWorks(Long projectId) {
        return workService.findAll(projectId, null);
    }

    public ProjectList createProject(ProjectListRequest request) {
        return projectService.create(request);
    }

    public ProjectList updateProject(Long projectId, ProjectListRequest request) {
        return projectService.update(projectId, request);
    }

    public void deleteProject(Long projectId) {
        projectService.delete(projectId);
    }

    public WorkList createWork(WorkListRequest request) {
        projectService.findById(request.projectId());
        return workService.create(request);
    }

    public WorkList updateWork(Long projectId, Long workId, WorkListRequest request) {
        requireProjectWork(projectId, workId);
        if (!projectId.equals(request.projectId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업무의 프로젝트는 변경할 수 없습니다.");
        }
        return workService.update(workId, request);
    }

    public void deleteWork(Long projectId, Long workId) {
        requireProjectWork(projectId, workId);
        workService.delete(workId);
    }

    private WorkList requireProjectWork(Long projectId, Long workId) {
        WorkList work = workService.findById(workId);
        if (!projectId.equals(work.getProjectId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "지정한 프로젝트에서 해당 업무를 찾을 수 없습니다."
            );
        }
        return work;
    }
}
