package com.app.mybackend.work.dto;

import java.time.LocalDate;

public record WorkListRequest(
        Long projectId,
        LocalDate workDate,
        String workTitle,
        String workCnnt,
        String workStatus
) {
}
