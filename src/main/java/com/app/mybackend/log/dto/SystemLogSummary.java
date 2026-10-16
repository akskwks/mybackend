package com.app.mybackend.log.dto;

import com.app.mybackend.log.entity.SystemLog;
import java.time.LocalDateTime;

public record SystemLogSummary(
        Long logId,
        String userName,
        String methodType,
        String apiUrl,
        String successYn,
        LocalDateTime requestAt,
        LocalDateTime responseAt
) {
    public static SystemLogSummary from(SystemLog log) {
        return new SystemLogSummary(log.getLogId(),
                log.getUser() == null ? "탈퇴한 사용자" : log.getUser().getUserName(),
                log.getMethodType(), log.getApiUrl(), log.getSuccessYn(),
                log.getRequestAt(), log.getResponseAt());
    }
}
