package com.app.mybackend.work.service;

import com.app.mybackend.work.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class WorkFileStorageService {

    private static final Logger log = LoggerFactory.getLogger(WorkFileStorageService.class);

    private final Path uploadRoot;

    public WorkFileStorageService(@Value("${myapp.file.upload-dir}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException exception) {
            throw new IllegalStateException("첨부파일 저장 경로를 생성할 수 없습니다.", exception);
        }
    }

    public SavedFile store(Long workId, MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "빈 파일은 첨부할 수 없습니다.");
        }

        String orgnFileName = FileUtils.sanitizeOriginalFileName(
                multipartFile.getOriginalFilename());
        String extension = FileUtils.extensionOf(orgnFileName);
        String savedName = FileUtils.createStoredFileName(extension);
        Path workDirectory = FileUtils.resolveInside(uploadRoot, String.valueOf(workId));
        Path target = FileUtils.resolveInside(workDirectory, savedName);

        try {
            Files.createDirectories(workDirectory);
            try (InputStream inputStream = multipartFile.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            String mimeType = FileUtils.resolveMimeType(
                    multipartFile.getContentType(), target);
            String relativePath = uploadRoot.relativize(target).toString().replace("\\", "/");
            return new SavedFile(orgnFileName, savedName, relativePath, extension, mimeType, multipartFile.getSize());
        } catch (IOException exception) {
            deleteQuietly(target);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "첨부파일을 저장하지 못했습니다.",
                    exception
            );
        }
    }

    public Resource load(String relativePath) {
        Path path = FileUtils.resolveInside(uploadRoot, relativePath);
        if (!Files.isRegularFile(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "첨부파일을 찾을 수 없습니다.");
        }
        return new FileSystemResource(path);
    }

    public void delete(String relativePath) {
        deleteQuietly(FileUtils.resolveInside(uploadRoot, relativePath));
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
            Path parent = path.getParent();
            if (parent != null && !parent.equals(uploadRoot) && Files.isDirectory(parent)) {
                try (var children = Files.list(parent)) {
                    if (children.findAny().isEmpty()) Files.deleteIfExists(parent);
                }
            }
        } catch (IOException exception) {
            log.warn("첨부파일 삭제에 실패했습니다: {}", path, exception);
        }
    }


    public record SavedFile(
            String orgnFileName,
            String svFileName,
            String filePath,
            String fileExtension,
            String mimeType,
            long fileSize
    ) {
    }
}
