package com.app.mybackend.log.dto;

import com.app.mybackend.log.entity.SystemLog;

/**
*
* 클래스명 : SystemLogDetail
* 역할 : 감사 로그의 요청·응답 JSON 상세 내용을 전달하는 DTO
* 생성일 : 2026-10-15
*
**/
public record SystemLogDetail(
        Long logId,
        String requestJson,
        String responseJson
) {
    public static SystemLogDetail from(SystemLog log) {
        return new SystemLogDetail(log.getLogId(), log.getRequestJson(), log.getResponseJson());
    }
}
