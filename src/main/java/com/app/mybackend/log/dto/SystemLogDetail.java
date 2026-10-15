package com.app.mybackend.log.dto;

import com.app.mybackend.log.entity.SystemLog;

public record SystemLogDetail(Long logId, String requestJson, String responseJson) {
    public static SystemLogDetail from(SystemLog log) {
        return new SystemLogDetail(log.getLogId(), log.getRequestJson(), log.getResponseJson());
    }
}
