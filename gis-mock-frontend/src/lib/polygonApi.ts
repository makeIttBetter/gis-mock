// src/lib/polygonApi.ts

import { apiGet, apiPost, apiDelete } from "@/lib/api";
import { PolygonDTO } from "@/interfaces/PolygonDTO";

/**
 * Fetch the list of saved polygons from the backend.
 */
export async function fetchPolygons(): Promise<PolygonDTO[]> {
    return apiGet<PolygonDTO[]>("POLYGONS");
}

/**
 * Save a new polygon with its name, coordinates, and real estate IDs.
 */
export async function createPolygon(
    name: string,
    coordinates: Array<{ lat: number; lng: number }>,
    realEstateIds: number[]
): Promise<PolygonDTO> {
    return apiPost<PolygonDTO>("POLYGONS", {
        name,
        coordinates,
        realEstateIds,
    });
}

/**
 * Delete a polygon by its ID.
 */
export async function deletePolygon(id: number): Promise<boolean> {
    return apiDelete("POLYGONS", id);
}
