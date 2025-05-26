import { apiGetPath, apiPostFormData } from "@/lib/api";
import { apiDeletePath } from "@/lib/api";  // <== if you define it in api.ts

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

    // POST to /api/real-estate/upload
    await apiPostFormData<void>("REAL_ESTATE", "upload", formData);
}

/**
 * Fetch the current CSV processing status from /api/real-estate/upload/status.
 */
export async function getCsvUploadStatus(): Promise<RealEstateCsvProcessingStatusDto> {
    return apiGetPath<RealEstateCsvProcessingStatusDto>("REAL_ESTATE", "upload/status");
}

/**
 * Abort (cancel) the current CSV processing by calling
 * DELETE /api/real-estate/upload/abort
 */
export async function abortCsvProcessing(): Promise<void> {
    await apiDeletePath("REAL_ESTATE", "upload/abort");
}
