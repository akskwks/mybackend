package com.app.mybackend.work.controller;

import com.app.mybackend.work.dto.WorkRequest;
import com.app.mybackend.work.entity.Work;
import com.app.mybackend.work.service.WorkService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/works")
public class WorkController {

    private final WorkService service;

    public WorkController(WorkService service) {
        this.service = service;
    }

    @GetMapping
    public List<Work> findAll(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return service.findAll(date);
    }

    @GetMapping("/{workId}")
    public Work findById(@PathVariable Long workId) {
        return service.findById(workId);
    }

    @PostMapping
    public Work create(@RequestBody WorkRequest request) {
        return service.create(request);
    }

    @PutMapping("/{workId}")
    public Work update(@PathVariable Long workId, @RequestBody WorkRequest request) {
        return service.update(workId, request);
    }

    @DeleteMapping("/{workId}")
    public void delete(@PathVariable Long workId) {
        service.delete(workId);
    }

}
