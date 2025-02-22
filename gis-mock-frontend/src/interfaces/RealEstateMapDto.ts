/**
 * src/interfaces/RealEstateMapDto.ts
 *
 * Updated to include extra fields that we need to edit in the marker pop-up:
 *  - soldTerms, soldPrice, taxId, address, zip
 *  - the rest of the fields remain the same
 */

export interface RealEstateMapDto {
    /** Internal DB ID (still used for linking). */
    id: string;

    /** MLS# as provided by the database. */
    mlsNumber?: string;

    /** Additional fields we need to edit right in the pop-up. */
    soldTerms?: string;
    soldPrice?: string;
    taxId?: string;
    address?: string;
    city?: string;
    state?: string;
    zip?: string;
    status?: string;

    /** Coordinates for placing a marker. */
    latitude?: number | null;
    longitude?: number | null;
}
