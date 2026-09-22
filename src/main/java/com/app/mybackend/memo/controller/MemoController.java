package com.app.mybackend.memo.controller;

import com.app.mybackend.memo.dto.MemoRequest;
import com.app.mybackend.memo.entity.Memo;
import com.app.mybackend.memo.service.MemoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memos")
public class MemoController {
    private final MemoService service;

    public MemoController(MemoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Memo> findAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String memoSort
    ) {
        return service.findAll(keyword, memoSort);
    }

    @GetMapping("/{memoId}")
    public Memo findById(@PathVariable Long memoId) {
        return service.findById(memoId);
    }

    @PostMapping
    public Memo create(@RequestBody MemoRequest request) {
        return service.create(request);
    }

    @PutMapping("/{memoId}")
    public Memo update(@PathVariable Long memoId, @RequestBody MemoRequest request) {
        return service.update(memoId, request);
    }

    @DeleteMapping("/{memoId}")
    public void delete(@PathVariable Long memoId) {
        service.delete(memoId);
    }
}
