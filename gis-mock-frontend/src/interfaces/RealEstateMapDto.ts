// src/interfaces/RealEstateMapDto.ts
export interface RealEstateMapDto {
    id: string;
    latitude: number | null;
    longitude: number | null;
    city: string;
    state: string;
    status: string;
}
