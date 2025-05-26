// File: src/lib/realEstateApi.ts
"use client";

import {apiGet} from "@/lib/api";
import {RealEstate} from "@/interfaces/RealEstate";
import {RealEstateFilterParams} from "@/interfaces/RealEstateFilterParams";
import {PaginatedResponse} from "@/interfaces/PaginatedResponse";
import {RealEstateMapDto} from "@/interfaces/RealEstateMapDto";
import {RealEstateUpdatePayload} from "@/interfaces/RealEstateUpdatePayload";

/**
 * Fetch all real estate (full fields) that match the filters.
 */
export async function fetchRealEstateData(
    filters?: RealEstateFilterParams
): Promise<RealEstate[]> {
    return apiGet<RealEstate[]>("REAL_ESTATE", filters);
}

/**
 * Fetch attached real estate data by a list of IDs.
 */
export async function fetchAttachedRealEstate(
    ids: string[]
): Promise<RealEstate[]> {
    return apiGet<RealEstate[]>("REAL_ESTATE_ATTACHED", {ids: ids.join(",")});
}

/**
 * Fetch a paginated list of RealEstate (full details).
 */
export async function fetchRealEstatePaginated(
    filters: RealEstateFilterParams,
    page: number,
    pageSize: number
): Promise<PaginatedResponse<RealEstate>> {
    const params: Record<string, any> = {...filters, page, pageSize};
    return apiGet<PaginatedResponse<RealEstate>>("REAL_ESTATE_PAGINATED", params);
}

/**
 * Fetch real estate map data with an optional polygon ID.
 * Returns an object with filtered and attached arrays.
 */
export async function fetchRealEstateMapData(
    filters: RealEstateFilterParams,
    polygonId?: string
): Promise<{ filtered: RealEstateMapDto[]; attached: RealEstateMapDto[] }> {
    const params = { ...filters, polygonId };
    return apiGet<{ filtered: RealEstateMapDto[]; attached: RealEstateMapDto[] }>(
        "REAL_ESTATE_MAP",
        params
    );
}


/**
 * PartialUpdate a RealEstate by ID.
 */
export async function updateRealEstate(
    id: string,
    payload: RealEstateUpdatePayload
): Promise<RealEstate> {
    const baseUrl = `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/real-estate/${id}`;
    const res = await fetch(baseUrl, {
        method: "PUT",
        headers: {"Content-Type": "application/json"},
        credentials: "include",
        body: JSON.stringify(payload),
    });
    if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.message || `Failed to update RealEstate ID: ${id}`);
    }
    return res.json();
}
