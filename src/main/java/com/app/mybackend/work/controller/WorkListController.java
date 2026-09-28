package com.app.mybackend.work.controller;

import com.app.mybackend.work.dto.WorkFileResponse;
import com.app.mybackend.work.dto.WorkListRequest;
import com.app.mybackend.work.entity.WorkList;
import com.app.mybackend.work.service.WorkFileService;
import com.app.mybackend.work.service.WorkListService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/works")
@RequiredArgsConstructor
public class WorkListController {

    private final WorkListService service;
    private final WorkFileService workFileService;

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

    @GetMapping("/{workId}/files")
    public List<WorkFileResponse> findFiles(@PathVariable Long workId) {
        service.findById(workId);
        return workFileService.findAll(workId);
    }

    @PostMapping
    public WorkList create(@RequestBody WorkListRequest request) {
        return service.create(request);
    }

    @PostMapping(value = "/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public WorkList createWithFiles(
            @RequestPart("work") WorkListRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return service.createWithFiles(request, files == null ? List.of() : files);
    }

    @PutMapping("/{workId}")
    public WorkList update(@PathVariable Long workId, @RequestBody WorkListRequest request) {
        return service.update(workId, request);
    }

    @PutMapping(value = "/{workId}/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public WorkList updateWithFiles(
            @PathVariable Long workId,
            @RequestPart("work") WorkListRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "deletedFileIds", required = false) List<Long> deletedFileIds
    ) {
        return service.updateWithFiles(
                workId,
                request,
                files == null ? List.of() : files,
                deletedFileIds == null ? List.of() : deletedFileIds
        );
    }

    @DeleteMapping("/{workId}")
    public void delete(@PathVariable Long workId) {
        service.delete(workId);
    }
}
