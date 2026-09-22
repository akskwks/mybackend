package com.app.mybackend.memo.repository;

import com.app.mybackend.memo.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    List<Memo> findByUpdatedAtGreaterThanEqualAndUpdatedAtLessThanOrderByUpdatedAtDesc(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
            SELECT memo
            FROM Memo memo
            WHERE (:keyword IS NULL
                OR LOWER(memo.memoTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(memo.memoCnnt) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:memoSort IS NULL OR memo.memoSort = :memoSort)
            ORDER BY memo.updatedAt DESC
            """)
    List<Memo> search(
            @Param("keyword") String keyword,
            @Param("memoSort") String memoSort
    );
}
