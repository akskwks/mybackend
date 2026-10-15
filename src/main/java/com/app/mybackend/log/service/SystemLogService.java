package com.app.mybackend.log.service;

import com.app.mybackend.log.dto.SystemLogDetail;
import com.app.mybackend.log.dto.SystemLogSummary;
import com.app.mybackend.log.entity.SystemLog;
import com.app.mybackend.log.repository.SystemLogRepository;
import com.app.mybackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SystemLogService {
    private final SystemLogRepository repository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userNum, String method, String url, boolean success,
                       String requestJson, LocalDateTime requestAt,
                       String responseJson, LocalDateTime responseAt) {
        String userId = userRepository.findById(userNum)
                .orElseThrow(() -> new IllegalStateException("로그 사용자를 찾을 수 없습니다."))
                .getUserId();
        SystemLog log = new SystemLog();
        log.setUserId(userId);
        log.setMethodType(method);
        log.setApiUrl(url);
        log.setSuccessYn(success ? "Y" : "N");
        log.setRequestJson(requestJson);
        log.setRequestAt(requestAt);
        log.setResponseJson(responseJson);
        log.setResponseAt(responseAt);
        repository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<SystemLogSummary> search(String keyword, String method, String success,
                                         int page, int size) {
        String normalizedKeyword = blankToNull(keyword);
        String normalizedMethod = blankToNull(method);
        String normalizedSuccess = blankToNull(success);
        if (normalizedMethod != null && !normalizedMethod.matches("GET|POST|PUT|PATCH|DELETE"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 메서드입니다.");
        if (normalizedSuccess != null && !normalizedSuccess.matches("Y|N"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 성공 여부입니다.");
        return repository.search(normalizedKeyword, normalizedMethod, normalizedSuccess,
                PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)),
                        Sort.by(Sort.Order.desc("requestAt"), Sort.Order.desc("logId"))))
                .map(SystemLogSummary::from);
    }

    @Transactional(readOnly = true)
    public SystemLogDetail detail(Long id) {
        return SystemLogDetail.from(repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "로그를 찾을 수 없습니다.")));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
