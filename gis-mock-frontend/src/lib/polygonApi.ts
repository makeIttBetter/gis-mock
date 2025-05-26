import {apiDelete, apiGet, apiGetPath, apiPost, apiPut} from "@/lib/api";
import {PolygonDTO} from "@/interfaces/PolygonDTO";
import {API_ENDPOINTS} from "@/config";

/**
 * Fetch all saved polygons.
 */
export async function fetchPolygons(): Promise<PolygonDTO[]> {
    return apiGet<PolygonDTO[]>("POLYGONS");
}

/**
 * Fetch a single polygon by its ID (calls GET /api/polygons/{id}).
 */
export async function fetchPolygonById(id: string): Promise<PolygonDTO> {
    return apiGetPath<PolygonDTO>("POLYGONS", id);
}

/**
 * Create a new polygon.
 */
export async function createPolygon(
    name: string,
    coordinates: Array<{ lat: number; lng: number }>,
    realEstateIds: string[]
): Promise<PolygonDTO> {
    return apiPost<PolygonDTO>("POLYGONS", {name, coordinates, realEstateIds});
}

/**
 * Update an existing polygon by ID.
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
 * Export the polygon's real estate data as CSV (returns a Blob).
 */
export async function exportPolygonCsv(polygonId: string): Promise<Blob> {
    const csvUrl = `${API_ENDPOINTS["POLYGONS"]}/${polygonId}/export/csv`;
    const response = await fetch(csvUrl, {
        method: "GET",
        credentials: "include",
    });
    if (!response.ok) {
        throw new Error("Failed to fetch CSV data from server.");
    }
    return await response.blob();
}
