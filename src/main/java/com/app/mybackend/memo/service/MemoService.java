package com.app.mybackend.memo.service;

import com.app.mybackend.memo.dto.MemoRequest;
import com.app.mybackend.memo.entity.Memo;
import com.app.mybackend.memo.repository.MemoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemoService {
    private final MemoRepository repository;

    public MemoService(MemoRepository repository) {
        this.repository = repository;
    }

    public List<Memo> findAll(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return repository.findAllByOrderByUpdatedAtDesc();
        }

        return repository.findByMemoTitleContainingIgnoreCaseOrMemoCnntContainingIgnoreCaseOrderByUpdatedAtDesc(keyword, keyword);
    }

    public Memo findById(Long memoId) {
        return repository.findById(memoId)
                .orElseThrow(() -> new RuntimeException("메모를 찾을 수 없습니다."));
    }

    public Memo create(MemoRequest request) {
        Memo memo = new Memo();
        apply(memo, request);
        return repository.save(memo);
    }

    public Memo update(Long memoId, MemoRequest request) {
        Memo memo = repository.findById(memoId)
                .orElseThrow(() -> new RuntimeException("메모를 찾을 수 없습니다."));
        apply(memo, request);
        return repository.save(memo);
    }

    public void delete(Long memoId) {
        repository.deleteById(memoId);
    }

    private void apply(Memo memo, MemoRequest request) {
        memo.setMemoTitle(request.memoTitle());
        memo.setMemoCnnt(request.memoCnnt());
        memo.setMemoSort(request.memoSort());
    }
}
