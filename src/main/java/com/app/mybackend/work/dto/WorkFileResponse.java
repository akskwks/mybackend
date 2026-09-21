package com.app.mybackend.work.dto;

import com.app.mybackend.work.entity.WorkFile;

import java.time.LocalDateTime;

public record WorkFileResponse(
        Long workFileId,
        Long workId,
        String orgnFileName,
        String fileExtension,
        String mimeType,
        Long fileSize,
        LocalDateTime createdAt
) {
    public static WorkFileResponse from(WorkFile file) {
        return new WorkFileResponse(
                file.getWorkFileId(),
                file.getWorkId(),
                file.getOrgnFileName(),
                file.getFileExtension(),
                file.getMimeType(),
                file.getFileSize(),
                file.getCreatedAt()
        );
    }
}
