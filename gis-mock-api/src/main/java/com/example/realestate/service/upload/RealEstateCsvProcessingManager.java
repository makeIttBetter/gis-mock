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
import java.util.concurrent.Future;

/**
 * Manages background CSV processing tasks for each user.
 * Supports partial progress + cancellation (abort).
 */
@Component
public class RealEstateCsvProcessingManager {

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

    public void startCsvProcessing(String userId, MultipartFile file) {
        // Create a new "task" object to track the job
        RealEstateCsvProcessingTask task = new RealEstateCsvProcessingTask(file);

        // Create a RealEstateCsvUploadResult to hold partial progress
        RealEstateCsvUploadResult partialResult = new RealEstateCsvUploadResult();
        task.setResult(partialResult);

        // Store the task
        tasksByUser.put(userId, task);

        // Instead of runAsync, we can do 'submit(...)' to get a Future
        Future<?> future = executor.submit(() -> {
            try {
                task.setStatus(RealEstateCsvTaskStatus.IN_PROGRESS);

                // Pass partialResult so we can see real-time updates
                realEstateCsvService.processCsv(file, partialResult);

                // If the above completes normally, mark as COMPLETED
                task.setStatus(RealEstateCsvTaskStatus.COMPLETED);

            } catch (Exception ex) {
                if (task.getStatus() == RealEstateCsvTaskStatus.IN_PROGRESS) {
                    // If an exception triggered while still in progress
                    task.setErrorMessage(ex.getMessage());
                    task.setStatus(RealEstateCsvTaskStatus.FAILED);
                }
            }
        });

        // Keep track of that Future so we can cancel it later
        task.setFuture(future);
    }

    /**
     * Abort an in-progress task by interrupting its thread.
     * This will cause 'Thread.currentThread().isInterrupted()' to be true
     * and stop further row processing (see RealEstateCsvService).
     */
    public void abortTask(String userId) {
        RealEstateCsvProcessingTask task = tasksByUser.get(userId);
        if (task == null) return;

        // If task is IN_PROGRESS, cancel it
        if (task.getStatus() == RealEstateCsvTaskStatus.IN_PROGRESS && task.getFuture() != null) {
            // Mark as FAILED or some ABORTED status
            task.setStatus(RealEstateCsvTaskStatus.FAILED);
            task.setErrorMessage("Aborted by user request.");

            // Cancel the Future => triggers thread interrupt
            task.getFuture().cancel(true);

            // remove from the map if you'd like:
            tasksByUser.remove(userId);
        }
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

        // If the task is done (either completed or failed), remove from the map
        if (!inProgress) {
            tasksByUser.remove(userId);
        }

        return dto;
    }
}
