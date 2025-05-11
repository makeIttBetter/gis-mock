import {apiPost} from "@/lib/api";
import {API_ENDPOINTS} from "@/config";

/**
 * Front-end helpers for Google OAuth & Sheets export.
 * Tokens live only on the backend – we just ask “is it valid?”.
 */
export interface GoogleAuthStatus {
    valid: boolean
}

/* ---------- STEP 1 – open popup & LOG the URL ---------- */
export function startGoogleOAuthFlow() {
    const authUrl = API_ENDPOINTS.GOOGLE_OAUTH_AUTHORIZE;
    console.log("[Google OAuth] opening authorization URL →", authUrl);

    const w = 600, h = 700;
    const left = window.screenX + (window.outerWidth - w) / 2;
    const top = window.screenY + (window.outerHeight - h) / 2;

    window.open(
        authUrl,
        "google-oauth",
        `width=${w},height=${h},left=${left},top=${top}`
    );
}

/* ---------- STEP 3 – token validity ---------- */
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

/* ---------- Export polygon ---------- */
export async function exportPolygonToGoogleSheets(polygonId: string): Promise<void> {
    await apiPost("GOOGLE_EXPORT_SHEETS", {polygonId});
}
