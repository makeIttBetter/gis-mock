import {API_ENDPOINTS} from "@/config";
import {ProfileResponseDto} from "@/interfaces/ProfileResponseDto";
import {ProfileRequestDto} from "@/interfaces/ProfileRequestDto";

/**
 * Fetch the current user's profile (GET /api/profile).
 */
export async function fetchProfile(): Promise<ProfileResponseDto> {
    const url = API_ENDPOINTS.PROFILE;
    const res = await fetch(url, {
        method: "GET",
        credentials: "include",
    });
    if (!res.ok) {
        throw new Error("Failed to load profile");
    }
    return res.json() as Promise<ProfileResponseDto>;
}

/**
 * Update the current user's profile (PUT /api/profile).
 * This can change the username or password or both.
 */
export async function updateProfile(payload: ProfileRequestDto): Promise<ProfileResponseDto> {
    const url = API_ENDPOINTS.PROFILE;
    const res = await fetch(url, {
        method: "PUT",
        credentials: "include",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
    });
    if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        const msg = errorData.message || "Failed to update profile.";
        throw new Error(msg);
    }
    return res.json();
}
