// src/interfaces/PolygonDTO.ts

/**
 * Represents a saved polygon and its related real estate objects.
 */
export interface PolygonDTO {
    id: string;
    name: string;
    // The set of latitude/longitude pairs that define the polygon boundary
    coordinates: Array<{ lat: number; lng: number }>;

    // List of real estate objects that were inside this polygon
    realEstateObjects: Array<string>; // You could store just IDs or RealEstate[] with details

    // Timestamps for display
    dateCreated: string;
    dateUpdated: string;
}

