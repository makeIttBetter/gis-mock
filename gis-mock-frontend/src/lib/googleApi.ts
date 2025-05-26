/**
 * src/lib/googleApi.ts
 *
 * Front-end helpers for Google OAuth and Google-Sheets export.
 * All tokens live on the backend – the browser only invokes the
 * endpoints and relays success / failure to the UI.
 */
"use client";

import {apiPost} from "@/lib/api";
import {API_ENDPOINTS} from "@/config";

/* ------------------------------------------------------------------ */
/* OAuth helpers                                                      */

/* ------------------------------------------------------------------ */

export interface GoogleAuthStatus {
    valid: boolean;
}

export function startGoogleOAuthFlow(): void {
    const authUrl = API_ENDPOINTS.GOOGLE_OAUTH_AUTHORIZE;
    const w = 600,
        h = 700;
    const left = window.screenX + (window.outerWidth - w) / 2;
    const top = window.screenY + (window.outerHeight - h) / 2;

    window.open(
        authUrl,
        "google-oauth",
        `width=${w},height=${h},left=${left},top=${top}`,
    );
}

export async function checkGoogleAuthStatus(): Promise<boolean> {
    try {
        const res = await fetch(API_ENDPOINTS.GOOGLE_OAUTH_VERIFY, {
            credentials: "include",
        });
        if (!res.ok) return false;
        const data = (await res.json()) as GoogleAuthStatus;
        return data.valid;
    } catch {
        return false;
    }
}

/* ------------------------------------------------------------------ */
/* Sheets export helpers                                              */

/* ------------------------------------------------------------------ */

export interface GoogleSheetsExportResponse {
    /** human-readable status from the backend */
    message: string;
}

/** Export a single polygon’s data to Google Sheets. */
export async function exportPolygonToGoogleSheets(
    polygonId: string,
): Promise<GoogleSheetsExportResponse> {
    return apiPost<GoogleSheetsExportResponse>("GOOGLE_EXPORT_SHEETS", {
        polygonId,
    });
}

/** Export multiple polygons to one sheet. */
export async function exportMultiplePolygonsToGoogleSheets(
    polygonIds: string[],
    sheetName: string,
): Promise<GoogleSheetsExportResponse> {
    return apiPost<GoogleSheetsExportResponse>("GOOGLE_EXPORT_SHEETS_MULTIPLE", {
        polygonIds,
        sheetName,
    });
}
