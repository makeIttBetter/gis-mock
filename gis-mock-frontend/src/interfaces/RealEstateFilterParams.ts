// frontend/src/interfaces/RealEstateFilterParams.ts
export interface RealEstateFilterParams {
    city?: string;
    state?: string;
    status?: string;
    minPrice?: string;
    maxPrice?: string;
    /** Comma-separated list of IDs */
    ids?: string;
    /** Full or partial address */
    address?: string;
    /** Zip code */
    zipcode?: string;
}
