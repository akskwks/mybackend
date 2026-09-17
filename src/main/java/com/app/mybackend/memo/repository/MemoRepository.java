package com.app.mybackend.memo.repository;

import com.app.mybackend.memo.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    List<Memo> findAllByOrderByUpdatedAtDesc();
    List<Memo> findByMemoTitleContainingIgnoreCaseOrMemoCnntContainingIgnoreCaseOrderByUpdatedAtDesc(
            String memoTitle,
            String memoCnnt
    );
    List<Memo> findByUpdatedAtGreaterThanEqualAndUpdatedAtLessThanOrderByUpdatedAtDesc(
            LocalDateTime start,
            LocalDateTime end
    );
}
