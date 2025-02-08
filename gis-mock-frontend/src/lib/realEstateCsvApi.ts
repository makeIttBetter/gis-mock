// File: src/lib/realEstateCsvApi.ts
import { apiPostFormData } from "@/lib/api";

/**
 * Upload CSV file.
 */
export async function uploadCsvFile(file: File): Promise<any> {
    const formData = new FormData();
    formData.append("file", file);
    // Use apiPostFormData to post to REAL_ESTATE/upload
    return apiPostFormData<any>("REAL_ESTATE", "upload", formData);
}
