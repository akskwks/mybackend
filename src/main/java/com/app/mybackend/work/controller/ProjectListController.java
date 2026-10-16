package com.app.mybackend.work.controller;

import com.app.mybackend.work.dto.ProjectListRequest;
import com.app.mybackend.work.entity.ProjectList;
import com.app.mybackend.work.service.ProjectListService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
*
* 클래스명 : ProjectListController
* 역할 : 프로젝트 목록 및 등록, 수정, 삭제 API를 제공하는 컨트롤러
* 생성일 : 2026-09-18
*
**/
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectListController {

    private final ProjectListService projectListService;

    @GetMapping
    public List<ProjectList> findAll() {
        return projectListService.findAll();
    }

    @PostMapping
    public ProjectList create(@RequestBody ProjectListRequest request) {
        return projectListService.create(request);
    }

    @PutMapping("/{projectId}")
    public ProjectList update(@PathVariable("projectId") Long projectId, @RequestBody ProjectListRequest request) {
        return projectListService.update(projectId, request);
    }

    @DeleteMapping("/{projectId}")
    public void delete(@PathVariable("projectId") Long projectId) {
        projectListService.delete(projectId);
    }

}
