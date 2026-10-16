package com.app.mybackend.log.repository;

import com.app.mybackend.log.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
*
* 클래스명 : SystemLogRepository
* 역할 : 감사 로그를 검색하고 저장하는 JPA 저장소
* 생성일 : 2026-10-15
*
**/
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    @Query(value = """
            SELECT l FROM SystemLog l LEFT JOIN FETCH l.user u
            WHERE (:keyword IS NULL OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(CONCAT(l.methodType, ' ', l.apiUrl)) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:methodType IS NULL OR l.methodType = :methodType)
              AND (:successYn IS NULL OR l.successYn = :successYn)
            """, countQuery = """
            SELECT COUNT(l) FROM SystemLog l LEFT JOIN l.user u
            WHERE (:keyword IS NULL OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(CONCAT(l.methodType, ' ', l.apiUrl)) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:methodType IS NULL OR l.methodType = :methodType)
              AND (:successYn IS NULL OR l.successYn = :successYn)
            """)
    Page<SystemLog> search(@Param("keyword") String keyword,
                           @Param("methodType") String methodType,
                           @Param("successYn") String successYn, Pageable pageable);
}
