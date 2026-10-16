package com.app.mybackend.work.dto;

import com.app.mybackend.work.entity.WorkFile;

import java.time.LocalDateTime;

/**
*
* 클래스명 : WorkFileResponse
* 역할 : 업무 첨부파일 정보를 클라이언트에 전달하는 응답 DTO
* 생성일 : 2026-09-21
*
**/
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
