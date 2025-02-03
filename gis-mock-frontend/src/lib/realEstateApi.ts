// File: frontend/src/lib/realEstateApi.ts
import { apiGet, apiPut } from "@/lib/api";
import { RealEstate } from "@/interfaces/RealEstate";
import { RealEstateFilterParams } from "@/interfaces/RealEstateFilterParams";

/**
 * Fetch all or filtered real estate data from the backend.
 */
export async function fetchRealEstateData(
    filters?: RealEstateFilterParams
): Promise<RealEstate[]> {
    return apiGet<RealEstate[]>("REAL_ESTATE", filters);
}

/**
 * Mark a real estate object as BASE.
 */
export async function markBaseObject(realEstateId: number): Promise<RealEstate> {
    return apiPut<RealEstate>("REAL_ESTATE", `base-object/${realEstateId}`, {});
}

/**
 * Fetch attached real estate data by a list of IDs.
 */
export async function fetchAttachedRealEstate(ids: string[]): Promise<RealEstate[]> {
    return apiGet<RealEstate[]>("REAL_ESTATE_ATTACHED", { ids: ids.join(",") });
}
