package com.app.mybackend.memo.repository;

import com.app.mybackend.memo.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    List<Memo> findByUserNumAndUpdatedAtGreaterThanEqualAndUpdatedAtLessThanOrderByUpdatedAtDesc(
            Long userNum,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<Memo> findByMemoIdAndUserNum(Long memoId, Long userNum);

    @Query("""
            SELECT memo
            FROM Memo memo
            WHERE memo.userNum = :userNum
              AND (:keyword IS NULL
                OR LOWER(memo.memoTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(memo.memoCnnt) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:memoSort IS NULL OR memo.memoSort = :memoSort)
            ORDER BY memo.updatedAt DESC
            """)
    List<Memo> search(
            @Param("userNum") Long userNum,
            @Param("keyword") String keyword,
            @Param("memoSort") String memoSort
    );
}
