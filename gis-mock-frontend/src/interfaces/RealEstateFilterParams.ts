// File: src/interfaces/RealEstateFilterParams.ts

export interface RealEstateFilterParams {
    city?: string;
    state?: string;
    status?: string;
    minPrice?: string;
    maxPrice?: string;
    ids?: string;
    address?: string;
    zipcode?: string;

    propertyTypes?: string[]; // e.g. ["Single Family", "Townhouse", ...]
    styles?: string[];        // e.g. ["Modern", "Traditional", ...]
    yearBuiltMin?: number;
    yearBuiltMax?: number;
    glaMin?: number;
    glaMax?: number;
    basementSqFtMin?: number;
    basementSqFtMax?: number;
    basementFinished?: boolean;
    daysBackMin?: number;
    daysBackMax?: number;
}
