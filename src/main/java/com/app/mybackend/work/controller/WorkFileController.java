package com.app.mybackend.work.controller;

import com.app.mybackend.work.entity.WorkFile;
import com.app.mybackend.work.service.WorkFileService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/work-files")
public class WorkFileController {

    private final WorkFileService workFileService;

    public WorkFileController(WorkFileService workFileService) {
        this.workFileService = workFileService;
    }

    @GetMapping("/{workFileId}/content")
    public ResponseEntity<Resource> content(@PathVariable Long workFileId) {
        return fileResponse(workFileId,false);
    }

    @GetMapping("/{workFileId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long workFileId) {
        return fileResponse(workFileId, true);
    }

    @DeleteMapping("/{workFileId}")
    public void delete(@PathVariable Long workFileId) {
        workFileService.deleteOne(workFileId);
    }

    private ResponseEntity<Resource> fileResponse(Long workFileId, boolean attachment) {
        WorkFile workFile = workFileService.findById(workFileId);
        Resource resource = workFileService.loadResource(workFile);
        MediaType mediaType;

        try {
            mediaType = MediaType.parseMediaType(workFile.getMimeType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        ContentDisposition contentDisposition = ContentDisposition
                .builder(attachment ? "attachment" : "inline")
                .filename(workFile.getOrgnFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(workFile.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }
}
