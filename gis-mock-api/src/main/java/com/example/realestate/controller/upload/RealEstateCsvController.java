package com.example.realestate.controller.upload;

import com.example.realestate.dto.upload.RealEstateCsvProcessingStatusDto;
import com.example.realestate.service.auth.SecurityService;
import com.example.realestate.service.upload.RealEstateCsvProcessingManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

/**
 * Controller for handling Real Estate CSV upload and upload status checks.
 */
@Slf4j
@RestController
@RequestMapping("/api/real-estate")
@RequiredArgsConstructor
public class RealEstateCsvController {

    private final RealEstateCsvProcessingManager processingManager;
    private final SecurityService securityService;

    /**
     * Initiates an asynchronous CSV processing task.
     * Returns 200 OK if the task starts successfully,
     * or 409 if a task is already running for the user.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadCsv(@RequestParam("file") MultipartFile file) {
        String userId = securityService.getCurrentUserId();

        if (processingManager.hasActiveTask(userId)) {
            log.warn("Upload request denied. Task already running for user: {}", userId);
            return ResponseEntity.status(409).body(
                    Collections.singletonMap("error", "A task is already running.")
            );
        }

        // Start asynchronous processing
        processingManager.startCsvProcessing(userId, file);

        // Return a minimal JSON to avoid parse errors
        return ResponseEntity.ok(Collections.singletonMap("message", "Upload started."));
    }

    /**
     * Returns the status of the CSV upload task (in progress, completed successfully, or failed).
     * If completed successfully, includes the RealEstateCsvUploadResult.
     * If no task is found for the user, indicates that no task is running.
     */
    @GetMapping("/upload/status")
    public ResponseEntity<RealEstateCsvProcessingStatusDto> getUploadStatus() {
        String userId = securityService.getCurrentUserId();
        RealEstateCsvProcessingStatusDto statusDto = processingManager.getStatusDto(userId);

        if (statusDto == null) {
            statusDto = new RealEstateCsvProcessingStatusDto(false, false, null, null);
        }

        return ResponseEntity.ok(statusDto);
    }

    @DeleteMapping("/upload/abort")
    public ResponseEntity<Map<String, String>> abortCsv() {
        String userId = securityService.getCurrentUserId();

        processingManager.abortTask(userId);

        return ResponseEntity.ok(Collections.singletonMap("message", "CSV processing aborted."));
    }

}
