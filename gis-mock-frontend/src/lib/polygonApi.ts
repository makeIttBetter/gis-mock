// File: src/lib/polygonApi.ts
import {apiDelete, apiGet, apiPost, apiPut} from "@/lib/api";
import {PolygonDTO} from "@/interfaces/PolygonDTO";
import {API_ENDPOINTS} from "@/config";

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
    return apiGet<PolygonDTO>("POLYGONS", {id});
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
 * Export the polygon's real estate data as CSV (returns a Blob).
 */
export async function exportPolygonCsv(polygonId: string): Promise<Blob> {
    // For CSV, we need a "blob" response.
    // We'll add a specialized function to api.ts or do fetch with standardized options:

    // Example if you had 'apiGetBlobPath' in api.ts:
    // return apiGetBlobPath("POLYGONS", `${polygonId}/export/csv`);

    // If not, we can do an inline fetch while still using your 'BACKEND_URL' etc.:
    // (We replicate the standard config: credentials: 'include')
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

