package com.app.mybackend.work.dto;

import java.time.LocalDate;

/**
*
* 클래스명 : WorkListRequest
* 역할 : 업무 등록 및 수정에 필요한 입력 데이터를 전달하는 요청 DTO
* 생성일 : 2026-09-18
*
**/
public record WorkListRequest(
        Long projectId,
        LocalDate workDate,
        String workTitle,
        String workCnnt,
        String workStatus
) {
}
