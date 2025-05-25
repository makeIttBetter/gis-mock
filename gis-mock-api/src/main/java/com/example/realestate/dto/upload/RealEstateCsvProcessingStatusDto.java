package com.example.realestate.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO returned by the /upload/status endpoint.
 * Indicates if a task is in progress, if it succeeded, any error, and the final result.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealEstateCsvProcessingStatusDto {
    private boolean inProgress;
    private boolean success;
    private String errorMessage;
    private RealEstateCsvUploadResult result;

}
