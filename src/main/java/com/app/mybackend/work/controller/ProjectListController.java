package com.app.mybackend.work.controller;

import com.app.mybackend.work.dto.ProjectListRequest;
import com.app.mybackend.work.entity.ProjectList;
import com.app.mybackend.work.service.ProjectListService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectListController {

    private final ProjectListService projectListService;

    public ProjectListController(ProjectListService projectListService) {
        this.projectListService = projectListService;
    }

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
