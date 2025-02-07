"use client";

import {apiGet} from "@/lib/api";
import {RealEstate} from "@/interfaces/RealEstate";
import {RealEstateFilterParams} from "@/interfaces/RealEstateFilterParams";
import {PaginatedResponse} from "@/interfaces/PaginatedResponse";
import {RealEstateMapDto} from "@/interfaces/RealEstateMapDto";

/**
 * Our standard fetch function used before. This returns ALL real estate
 * (full fields) that match the filters.  (Kept for older usage.)
 */
export async function fetchRealEstateData(
    filters?: RealEstateFilterParams
): Promise<RealEstate[]> {
    return apiGet<RealEstate[]>("REAL_ESTATE", filters);
}

/**
 * Fetch attached real estate data by a list of IDs (unchanged).
 */
export async function fetchAttachedRealEstate(
    ids: string[]
): Promise<RealEstate[]> {
    return apiGet<RealEstate[]>("REAL_ESTATE_ATTACHED", {ids: ids.join(",")});
}

/**
 * The function to fetch a PAGINATED list of RealEstate (full details).
 */
export async function fetchRealEstatePaginated(
    filters: RealEstateFilterParams,
    page: number,
    pageSize: number
): Promise<PaginatedResponse<RealEstate>> {
    // Build query params
    const params: Record<string, any> = {...filters, page, pageSize};
    return apiGet<PaginatedResponse<RealEstate>>("REAL_ESTATE_PAGINATED", params);
}

/**
 */
export async function fetchRealEstateMapData(
    filters: RealEstateFilterParams
): Promise<RealEstateMapDto[]> {
    return apiGet<RealEstateMapDto[]>("REAL_ESTATE_MAP", filters);
}
