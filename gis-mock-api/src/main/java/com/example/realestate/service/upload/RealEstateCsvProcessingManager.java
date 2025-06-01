package com.example.realestate.service.upload;

import com.example.realestate.dto.upload.RealEstateCsvProcessingStatusDto;
import com.example.realestate.dto.upload.RealEstateCsvUploadResult;
import com.example.realestate.dto.upload.tasks.RealEstateCsvProcessingTask;
import com.example.realestate.dto.upload.tasks.RealEstateCsvTaskStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Manages background CSV processing tasks for each user.
 * Now makes an in-memory copy of the uploaded file so the async thread
 * can still read it after the HTTP request finishes.
 */
@Component
public class RealEstateCsvProcessingManager {

    /* ────────────────────────────────────────────────────────────────
       Helper: a simple in-memory implementation of MultipartFile
       ──────────────────────────────────────────────────────────────── */
    private static class InMemoryMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        InMemoryMultipartFile(String name,
                              String originalFilename,
                              String contentType,
                              byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = (content != null) ? content : new byte[0];
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), content);
        }
    }
    /* ──────────────────────────────────────────────────────────────── */

    private final RealEstateCsvService realEstateCsvService;
    private final ThreadPoolTaskExecutor executor;
    private final Map<String, RealEstateCsvProcessingTask> tasksByUser = new ConcurrentHashMap<>();

    public RealEstateCsvProcessingManager(RealEstateCsvService realEstateCsvService) {
        this.realEstateCsvService = realEstateCsvService;

        this.executor = new ThreadPoolTaskExecutor();
        this.executor.setCorePoolSize(5);
        this.executor.setMaxPoolSize(10);
        this.executor.setQueueCapacity(50);
        this.executor.initialize();
    }

    public boolean hasActiveTask(String userId) {
        RealEstateCsvProcessingTask task = tasksByUser.get(userId);
        return task != null && task.getStatus() == RealEstateCsvTaskStatus.IN_PROGRESS;
    }

    public void startCsvProcessing(String userId, MultipartFile multipartFile) {

        /* 1. Make a private, safe copy of the file bytes */
        byte[] bytes;
        try {
            bytes = multipartFile.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Unable to read uploaded file", e);
        }

        /* 2. Wrap those bytes in an in-memory MultipartFile */
        MultipartFile safeCopy = new InMemoryMultipartFile(
                multipartFile.getName(),
                multipartFile.getOriginalFilename(),
                multipartFile.getContentType(),
                bytes
        );

        /* 3. Create & store the task object */
        RealEstateCsvProcessingTask task = new RealEstateCsvProcessingTask(safeCopy);
        RealEstateCsvUploadResult partialResult = new RealEstateCsvUploadResult();
        task.setResult(partialResult);
        tasksByUser.put(userId, task);

        /* 4. Launch the async work */
        Future<?> future = executor.submit(() -> {
            try {
                task.setStatus(RealEstateCsvTaskStatus.IN_PROGRESS);
                realEstateCsvService.processCsv(safeCopy, partialResult);
                task.setStatus(RealEstateCsvTaskStatus.COMPLETED);
            } catch (Exception ex) {
                // Only update if we were still in progress
                if (task.getStatus() == RealEstateCsvTaskStatus.IN_PROGRESS) {
                    task.setErrorMessage(ex.getMessage());
                    task.setStatus(RealEstateCsvTaskStatus.FAILED);
                }
            }
        });

        /* 5. Keep reference to cancel later if needed */
        task.setFuture(future);
    }

    /**
     * Abort logic unchanged
     */
    public void abortTask(String userId) {
        RealEstateCsvProcessingTask task = tasksByUser.get(userId);
        if (task == null) return;

        if (task.getStatus() == RealEstateCsvTaskStatus.IN_PROGRESS && task.getFuture() != null) {
            task.setStatus(RealEstateCsvTaskStatus.FAILED);
            task.setErrorMessage("Aborted by user request.");
            task.getFuture().cancel(true);
            tasksByUser.remove(userId);
        }
    }

    /**
     * Status lookup unchanged
     */
    public RealEstateCsvProcessingStatusDto getStatusDto(String userId) {
        RealEstateCsvProcessingTask task = tasksByUser.get(userId);
        if (task == null) {
            return null;
        }

        boolean inProgress = (task.getStatus() == RealEstateCsvTaskStatus.IN_PROGRESS);
        boolean success = (task.getStatus() == RealEstateCsvTaskStatus.COMPLETED);
        String error = (task.getStatus() == RealEstateCsvTaskStatus.FAILED)
                ? task.getErrorMessage()
                : null;

        RealEstateCsvProcessingStatusDto dto = new RealEstateCsvProcessingStatusDto(
                inProgress, success, error, task.getResult()
        );

        if (!inProgress) {
            tasksByUser.remove(userId);
        }
        return dto;
    }
}
