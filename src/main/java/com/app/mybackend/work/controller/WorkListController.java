package com.app.mybackend.work.controller;

import com.app.mybackend.work.dto.WorkListRequest;
import com.app.mybackend.work.entity.WorkList;
import com.app.mybackend.work.service.WorkListService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/works")
public class WorkListController {

    private final WorkListService service;

    public WorkListController(WorkListService service) {
        this.service = service;
    }

    @GetMapping
    public List<WorkList> findAll(
            @RequestParam Long projectId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return service.findAll(projectId, date);
    }

    @GetMapping("/{workId}")
    public WorkList findById(@PathVariable Long workId) {
        return service.findById(workId);
    }

    @PostMapping
    public WorkList create(@RequestBody WorkListRequest request) {
        return service.create(request);
    }

    @PutMapping("/{workId}")
    public WorkList update(@PathVariable Long workId, @RequestBody WorkListRequest request) {
        return service.update(workId, request);
    }

    @DeleteMapping("/{workId}")
    public void delete(@PathVariable Long workId) {
        service.delete(workId);
    }

}
