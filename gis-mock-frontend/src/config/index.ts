/*
  Developer note: This config defines the list of constants used in the application.
  Adjust the BACKEND_URL as necessary for different environments.
*/
export const BACKEND_URL: string =
    process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

export const API_ENDPOINTS = {
    REAL_ESTATE: `${BACKEND_URL}/api/real-estate`,
    POLYGONS: `${BACKEND_URL}/api/polygons`,
    REAL_ESTATE_ATTACHED: `${BACKEND_URL}/api/real-estate/attached`,
    REAL_ESTATE_FILTERS: `${BACKEND_URL}/api/real-estate/filters`
    // ... add other endpoints as needed.
};
