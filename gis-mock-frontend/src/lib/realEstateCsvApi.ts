import { API_ENDPOINTS } from "@/config";

export async function uploadCsvFile(file: File): Promise<any> {
    const formData = new FormData();
    formData.append("file", file);
    const response = await fetch(`${API_ENDPOINTS.REAL_ESTATE}/upload`, {
        method: "POST",
        body: formData,
    });
    if (!response.ok) {
        throw new Error(`Upload failed: ${response.status}`);
    }
    return response.json();
}
