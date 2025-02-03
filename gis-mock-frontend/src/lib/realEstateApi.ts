import {apiGet, apiPut} from "@/lib/api";
import { RealEstate } from "@/interfaces/RealEstate";
import { RealEstateFilterParams } from "@/interfaces/RealEstateFilterParams";

/**
 * Fetch all or filtered real estate data from the backend.
 */
export async function fetchRealEstateData(
    filters?: RealEstateFilterParams
): Promise<RealEstate[]> {
    // If filters is empty, this effectively calls /api/real-estate without query params
    return apiGet<RealEstate[]>("REAL_ESTATE", filters);
}

/**
 * Mark a real estate object as BASE.
 * We assume your backend automatically unmarks the old BASE if there was one.
 */
export async function markBaseObject(realEstateId: number): Promise<RealEstate> {
    // For example, your backend might have a dedicated endpoint:
    // PUT /api/real-estate/base-object/{id}
    // Here we show a simple approach:
    return apiPut<RealEstate>("REAL_ESTATE", `base-object/${realEstateId}`, {});
}