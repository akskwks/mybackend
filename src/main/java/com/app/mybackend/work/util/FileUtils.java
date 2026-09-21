package com.app.mybackend.work.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

public final class FileUtils {

    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    private FileUtils() {
    }

    public static String sanitizeOriginalFileName(String orgnFileName) {
        String normalized = orgnFileName == null
                ? ""
                : orgnFileName.replace('\\', '/');
        String fileName = normalized.substring(normalized.lastIndexOf('/') + 1).trim();

        if (fileName.isBlank()
                || fileName.length() > 255
                || fileName.chars().anyMatch(Character::isISOControl)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일명을 확인해 주세요.");
        }
        return fileName;
    }

    public static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 1 || dot == fileName.length() - 1) return "";

        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (extension.length() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일 확장자가 너무 깁니다.");
        }
        return extension;
    }

    public static String createStoredFileName(String extension) {
        String identifier = UUID.randomUUID().toString();
        return extension == null || extension.isBlank()
                ? identifier
                : identifier + "." + extension;
    }

    public static String resolveMimeType(String declaredMimeType, Path storedFile) throws IOException {
        if (declaredMimeType != null && !declaredMimeType.isBlank()) {
            return declaredMimeType;
        }
        String detected = Files.probeContentType(storedFile);
        return detected == null || detected.isBlank()
                ? DEFAULT_MIME_TYPE
                : detected;
    }

    public static Path resolveInside(Path root, String relativePath) {
        Path resolved = root.resolve(relativePath).normalize();
        if (!resolved.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 파일 경로입니다.");
        }
        return resolved;
    }
}
