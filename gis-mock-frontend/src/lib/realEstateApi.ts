// File: src/lib/realEstateApi.ts
"use client";

import { apiGet } from "@/lib/api";
import { RealEstate } from "@/interfaces/RealEstate";
import { RealEstateFilterParams } from "@/interfaces/RealEstateFilterParams";
import { PaginatedResponse } from "@/interfaces/PaginatedResponse";
import { RealEstateMapDto } from "@/interfaces/RealEstateMapDto";

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
    return apiGet<RealEstate[]>("REAL_ESTATE_ATTACHED", { ids: ids.join(",") });
}

/**
 * Fetch a paginated list of RealEstate (full details).
 */
export async function fetchRealEstatePaginated(
    filters: RealEstateFilterParams,
    page: number,
    pageSize: number
): Promise<PaginatedResponse<RealEstate>> {
    const params: Record<string, any> = { ...filters, page, pageSize };
    return apiGet<PaginatedResponse<RealEstate>>("REAL_ESTATE_PAGINATED", params);
}

/**
 * Fetch minimal real estate data for the map.
 */
export async function fetchRealEstateMapData(
    filters: RealEstateFilterParams
): Promise<RealEstateMapDto[]> {
    return apiGet<RealEstateMapDto[]>("REAL_ESTATE_MAP", filters);
}
