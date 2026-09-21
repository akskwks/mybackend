package com.app.mybackend.work.service;

import com.app.mybackend.work.dto.WorkFileResponse;
import com.app.mybackend.work.entity.WorkFile;
import com.app.mybackend.work.repository.WorkFileRepository;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class WorkFileService {

    private static final int MAX_FILES_PER_REQUEST = 20;

    private final WorkFileRepository workFileRepository;
    private final WorkFileStorageService workFileStorageService;

    public WorkFileService(WorkFileRepository workFileRepository, WorkFileStorageService workFileStorageService) {
        this.workFileRepository = workFileRepository;
        this.workFileStorageService = workFileStorageService;
    }

    @Transactional(readOnly = true)
    public List<WorkFileResponse> findAll(Long workId) {
        return workFileRepository.findByWorkIdOrderByCreatedAtAsc(workId)
                .stream()
                .map(WorkFileResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkFile findById(Long workFileId) {
        return workFileRepository.findById(workFileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "첨부파일을 찾을 수 없습니다."
                ));
    }

    public List<WorkFile> saveAll(Long workId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();
        if (files.size() > MAX_FILES_PER_REQUEST) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "한 번에 최대 20개까지 첨부할 수 있습니다."
            );
        }

        List<String> storedPaths = new ArrayList<>();
        List<WorkFile> entities = new ArrayList<>();
        try {
            for (MultipartFile multipartFile : files) {
                WorkFileStorageService.SavedFile saved = workFileStorageService.store(workId, multipartFile);
                storedPaths.add(saved.filePath());

                WorkFile entity = new WorkFile();
                entity.setWorkId(workId);
                entity.setOrgnFileName(saved.orgnFileName());
                entity.setSvFileName(saved.svFileName());
                entity.setFilePath(saved.filePath());
                entity.setFileExtension(saved.fileExtension());
                entity.setMimeType(saved.mimeType());
                entity.setFileSize(saved.fileSize());
                entities.add(entity);
            }
            List<WorkFile> saved = workFileRepository.saveAll(entities);
            deleteNewFilesOnRollback(storedPaths);
            return saved;
        } catch (RuntimeException exception) {
            storedPaths.forEach(workFileStorageService::delete);
            throw exception;
        }
    }

    public void deleteSelected(Long workId, Collection<Long> workFileIds) {
        if (workFileIds == null || workFileIds.isEmpty()) return;

        List<WorkFile> workFiles = workFileRepository.findByWorkIdAndWorkFileIdIn(workId, workFileIds);
        if (workFiles.size() != workFileIds.stream().distinct().count()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "삭제할 첨부파일 정보를 확인해주세요."
            );
        }
        workFileRepository.deleteAll(workFiles);
        deletePhysicalFilesAfterCommit(workFiles);
    }

    public void deleteAllForWork(Long workId) {
        List<WorkFile> files = workFileRepository.findByWorkIdOrderByCreatedAtAsc(workId);
        workFileRepository.deleteAll(files);
        deletePhysicalFilesAfterCommit(files);
    }

    @Transactional
    public void deleteOne(Long workFileId) {
        WorkFile file = findById(workFileId);
        workFileRepository.delete(file);
        deletePhysicalFilesAfterCommit(List.of(file));
    }

    public Resource loadResource(WorkFile file) {
        return workFileStorageService.load(file.getFilePath());
    }

    private void deleteNewFilesOnRollback(List<String> paths) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    paths.forEach(workFileStorageService::delete);
                }
            }
        });
    }

    private void deletePhysicalFilesAfterCommit(List<WorkFile> files) {
        Runnable deleteFiles = () -> files.stream()
                .map(WorkFile::getFilePath)
                .forEach(workFileStorageService::delete);

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteFiles.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteFiles.run();
            }
        });
    }
}
