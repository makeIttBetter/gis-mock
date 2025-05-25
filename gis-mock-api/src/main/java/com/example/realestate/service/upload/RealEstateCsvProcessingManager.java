package com.example.realestate.service.upload;

import com.example.realestate.dto.upload.RealEstateCsvProcessingStatusDto;
import com.example.realestate.dto.upload.RealEstateCsvUploadResult;
import com.example.realestate.dto.upload.tasks.RealEstateCsvProcessingTask;
import com.example.realestate.dto.upload.tasks.RealEstateCsvTaskStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages background CSV processing tasks for each user.
 */
@Component
public class RealEstateCsvProcessingManager {

    private final RealEstateCsvService realEstateCsvService;
    private final ThreadPoolTaskExecutor executor;
    private final Map<String, RealEstateCsvProcessingTask> tasksByUser = new ConcurrentHashMap<>();

    public RealEstateCsvProcessingManager(RealEstateCsvService realEstateCsvService) {
        this.realEstateCsvService = realEstateCsvService;

        // A ThreadPoolTaskExecutor can run tasks asynchronously.
        // Configure it as needed (corePoolSize, maxPoolSize, etc).
        this.executor = new ThreadPoolTaskExecutor();
        this.executor.setCorePoolSize(5);
        this.executor.setMaxPoolSize(10);
        this.executor.setQueueCapacity(50);
        this.executor.initialize();
    }

    /**
     * Checks if a task is already active for the given user.
     */
    public boolean hasActiveTask(String userId) {
        RealEstateCsvProcessingTask task = tasksByUser.get(userId);
        return task != null && task.getStatus() == RealEstateCsvTaskStatus.IN_PROGRESS;
    }

    /**
     * Starts a new CSV processing task for the user.
     * If a task was previously completed or failed, it is replaced.
     */
    public void startCsvProcessing(String userId, MultipartFile file) {
        RealEstateCsvProcessingTask task = new RealEstateCsvProcessingTask(file);
        tasksByUser.put(userId, task);

        executor.submit(() -> {
            try {
                task.setStatus(RealEstateCsvTaskStatus.IN_PROGRESS);
                RealEstateCsvUploadResult result = realEstateCsvService.processCsv(file);
                task.setResult(result);
                task.setStatus(RealEstateCsvTaskStatus.COMPLETED);
            } catch (Exception ex) {
                task.setErrorMessage(ex.getMessage());
                task.setStatus(RealEstateCsvTaskStatus.FAILED);
            }
        });
    }

    /**
     * Returns the status/result of the user’s CSV processing task.
     * Removes the task from memory if it is no longer in progress.
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
                inProgress,
                success,
                error,
                task.getResult()
        );

        // If the task is done (either completed or failed), remove it from the map
        if (!inProgress) {
            tasksByUser.remove(userId);
        }

        return dto;
    }
}
