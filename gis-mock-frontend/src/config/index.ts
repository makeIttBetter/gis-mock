/*
  Centralised runtime-config.

  BACKEND_URL falls back to localhost when the env-var is missing,
  so “npm run dev” still works out-of-the-box.
*/
export const BACKEND_URL: string =
    process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

export const API_ENDPOINTS = {
    /* ------- domain: polygons ------- */
    POLYGONS: `${BACKEND_URL}/api/polygons`,

    /* ------- domain: real-estate ------- */
    REAL_ESTATE: `${BACKEND_URL}/api/real-estate`,
    REAL_ESTATE_ATTACHED: `${BACKEND_URL}/api/real-estate/attached`,
    REAL_ESTATE_FILTERS: `${BACKEND_URL}/api/real-estate/filters`,
    REAL_ESTATE_PAGINATED: `${BACKEND_URL}/api/real-estate/paginated`,
    REAL_ESTATE_MAP: `${BACKEND_URL}/api/real-estate/map`,

    /* ------- auth / profile ------- */
    AUTH_VERIFY: `${BACKEND_URL}/api/auth/verify`,
    PROFILE: `${BACKEND_URL}/api/profile`,

    /* ------- NEW: google ------- */
    GOOGLE_EXPORT_SHEETS: `${BACKEND_URL}/api/google/sheets/export`,
    GOOGLE_EXPORT_SHEETS_MULTIPLE: `${BACKEND_URL}/api/google/sheets/export-multiple`,
    GOOGLE_OAUTH_AUTHORIZE: `${BACKEND_URL}/api/google/oauth2/authorize`,
    GOOGLE_OAUTH_VERIFY: `${BACKEND_URL}/api/google/oauth2/verify`,
} as const;
