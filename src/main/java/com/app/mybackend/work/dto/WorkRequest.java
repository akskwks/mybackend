package com.app.mybackend.work.dto;

import java.time.LocalDate;

public record WorkRequest(
        LocalDate workDate,
        String workTitle,
        String workCnnt,
        String workStatus,
        Integer workProgress
) {
}
