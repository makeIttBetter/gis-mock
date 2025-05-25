// File: src/lib/realEstateCsvApi.ts
import {apiGetPath, apiPostFormData} from "@/lib/api";

/**
 * Shape of the CSV error item.
 */
export interface CsvError {
    rowNumber: number;
    mlsNumber?: string;
    address?: string;
    errorMessage: string;
}

/**
 * Represents the CSV upload result fields.
 */
export interface RealEstateCsvUploadResult {
    totalRows: number;
    processedRows: number;
    updatedCount: number;
    createdCount: number;
    errors: CsvError[];
    headerRow?: string[];
    unsavedRows?: string[][];
}

/**
 * Represents the status response from /api/real-estate/upload/status.
 */
export interface RealEstateCsvProcessingStatusDto {
    inProgress: boolean;
    success: boolean;
    errorMessage: string | null;
    result: RealEstateCsvUploadResult | null;
}

/**
 * Upload CSV file (starts the async processing).
 */
export async function uploadCsvFile(file: File): Promise<void> {
    const formData = new FormData();
    formData.append("file", file);

    // Uses the generic apiPostFormData to POST to /api/real-estate/upload
    // The endpoint "REAL_ESTATE" is mapped to "/api/real-estate"
    // Then we add the path "upload".
    await apiPostFormData<void>("REAL_ESTATE", "upload", formData);
}

/**
 * Fetch the current CSV processing status from /api/real-estate/upload/status.
 */
export async function getCsvUploadStatus(): Promise<RealEstateCsvProcessingStatusDto> {
    // Using apiGetPath to call GET /api/real-estate/upload/status
    return apiGetPath<RealEstateCsvProcessingStatusDto>("REAL_ESTATE", "upload/status");
}
