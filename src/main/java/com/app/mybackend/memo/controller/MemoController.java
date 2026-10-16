package com.app.mybackend.memo.controller;

import com.app.mybackend.memo.dto.MemoRequest;
import com.app.mybackend.memo.entity.Memo;
import com.app.mybackend.memo.service.MemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
*
* 클래스명 : MemoController
* 역할 : 메모 목록, 상세 및 등록, 수정, 삭제 API를 제공하는 컨트롤러
* 생성일 : 2026-09-17
*
**/
@RestController
@RequestMapping("/api/memos")
@RequiredArgsConstructor
public class MemoController {

    private final MemoService service;

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
