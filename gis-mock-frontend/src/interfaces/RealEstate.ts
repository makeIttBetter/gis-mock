// Represents the real estate object returned from the backend
export interface RealEstate {
    id: number;
    mlsNumber: string;
    address: string;
    city: string;
    state: string;
    zip: string;
    status: string;
    listPrice: string;
    latitude: string;
    longitude: string;
}
