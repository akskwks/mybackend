package com.app.mybackend.log.controller;

import com.app.mybackend.log.dto.SystemLogDetail;
import com.app.mybackend.log.dto.SystemLogSummary;
import com.app.mybackend.log.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class SystemLogController {
    private final SystemLogService service;

    @GetMapping
    public Page<SystemLogSummary> search(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String methodType,
                                         @RequestParam(required = false) String successYn,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "50") int size) {
        return service.search(keyword, methodType, successYn, page, size);
    }

    @GetMapping("/{logId}")
    public SystemLogDetail detail(@PathVariable Long logId) {
        return service.detail(logId);
    }
}
