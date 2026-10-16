package com.app.mybackend.memo.service;

import com.app.mybackend.memo.dto.MemoRequest;
import com.app.mybackend.memo.entity.Memo;
import com.app.mybackend.memo.repository.MemoRepository;
import com.app.mybackend.config.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
*
* 클래스명 : MemoService
* 역할 : 사용자별 메모 검색, 조회, 등록, 수정, 삭제를 처리하는 서비스
* 생성일 : 2026-09-17
*
**/
@Service
public class MemoService {
    private final MemoRepository repository;
    private final CurrentUser currentUser;

    public MemoService(MemoRepository repository, CurrentUser currentUser) {
        this.repository = repository;
        this.currentUser = currentUser;
    }

    public List<Memo> findAll(String keyword) {
        return findAll(keyword, null);
    }

    public List<Memo> findAll(String keyword, String memoSort) {
        String normalizedKeyword = normalize(keyword);
        String normalizedMemoSort = normalize(memoSort);
        return repository.search(currentUser.number(), normalizedKeyword, normalizedMemoSort);
    }

    public Memo findById(Long memoId) {
        return repository.findByMemoIdAndUserNum(memoId, currentUser.number())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "메모를 찾을 수 없습니다."));
    }

    public List<Memo> findUpdatedBetween(LocalDateTime start, LocalDateTime end) {
        return repository.findByUserNumAndUpdatedAtGreaterThanEqualAndUpdatedAtLessThanOrderByUpdatedAtDesc(
                currentUser.number(), start, end);
    }

    public Memo create(MemoRequest request) {
        Memo memo = new Memo();
        memo.setUserNum(currentUser.number());
        apply(memo, request);
        return repository.save(memo);
    }

    public Memo update(Long memoId, MemoRequest request) {
        Memo memo = findById(memoId);
        apply(memo, request);
        return repository.save(memo);
    }

    public void delete(Long memoId) {
        repository.delete(findById(memoId));
    }

    private void apply(Memo memo, MemoRequest request) {
        memo.setMemoTitle(request.memoTitle());
        memo.setMemoCnnt(request.memoCnnt());
        memo.setMemoSort(request.memoSort());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
