// File: src/lib/polygonApi.ts
import { apiGet, apiPost, apiPut, apiDelete, apiPostPath } from "@/lib/api";
import { PolygonDTO } from "@/interfaces/PolygonDTO";

/**
 * Fetch all saved polygons.
 */
export async function fetchPolygons(): Promise<PolygonDTO[]> {
    return apiGet<PolygonDTO[]>("POLYGONS");
}

/**
 * Fetch a single polygon by its ID.
 */
export async function fetchPolygonById(id: string): Promise<PolygonDTO> {
    return apiGet<PolygonDTO>("POLYGONS", { id });
}

/**
 * Create a new polygon.
 */
export async function createPolygon(
    name: string,
    coordinates: Array<{ lat: number; lng: number }>,
    realEstateIds: string[]
): Promise<PolygonDTO> {
    return apiPost<PolygonDTO>("POLYGONS", { name, coordinates, realEstateIds });
}

/**
 * Update an existing polygon.
 */
export async function updatePolygon(
    id: string,
    data: { name: string; coordinates: { lat: number; lng: number }[]; realEstateIds: string[] }
): Promise<PolygonDTO> {
    return apiPut<PolygonDTO>("POLYGONS", id, data);
}

/**
 * Delete a polygon by its ID.
 */
export async function deletePolygon(id: string): Promise<boolean> {
    return apiDelete("POLYGONS", id);
}

/**
 * Export the attached real estate objects to Google Sheets, returning the final sheet URL.
 */
export async function exportPolygonToGoogleSheets(id: string): Promise<string> {
    // Use apiPostPath: base endpoint is POLYGONS and we append `${id}/export/google-sheets`
    const response = await apiPostPath<{ sheetUrl: string }>("POLYGONS", `${id}/export/google-sheets`, {});
    return response.sheetUrl;
}

