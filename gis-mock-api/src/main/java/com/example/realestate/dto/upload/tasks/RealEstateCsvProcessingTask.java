package com.example.realestate.dto.upload.tasks;

import com.example.realestate.dto.upload.RealEstateCsvUploadResult;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * Holds data about a single CSV processing task, including
 * status, result, and error messages.
 */
@Data
public class RealEstateCsvProcessingTask {

    private final MultipartFile file;
    private RealEstateCsvTaskStatus status;
    private RealEstateCsvUploadResult result;
    private String errorMessage;

    public RealEstateCsvProcessingTask(MultipartFile file) {
        this.file = file;
        this.status = RealEstateCsvTaskStatus.PENDING;
    }
}
