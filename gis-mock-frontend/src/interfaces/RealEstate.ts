// File: src/interfaces/RealEstate.ts

export interface RealEstate {
    id: string;
    mlsNumber: string;
    taxId?: string;
    address: string;
    city: string;
    state: string;
    zip: string;
    status: string;
    listPrice: string;
    latitude: string;
    longitude: string;

    // OPTIONAL Fields we now support editing from the map pop-up:
    soldTerms?: string;
    soldPrice?: string;
}
