// File: src/interfaces/RealEstateUpdatePayload.ts

/**
 * Contains the allowed fields to update from the map pop-up forms.
 */
export interface RealEstateUpdatePayload {
    soldTerms?: string | null;
    soldPrice?: string | null;
    mlsNumber?: string | null;
    taxId?: string | null;
    address?: string | null;
    city?: string | null;
    state?: string | null;
    zip?: string | null;
    status?: string | null;
}
