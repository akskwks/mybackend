package com.app.mybackend.work.dto;

import java.time.LocalDate;

public record ProjectListRequest(
        String workEnvironment,
        String projectName,
        String projectStatus,
        LocalDate startDate,
        LocalDate endDate
) {
}
