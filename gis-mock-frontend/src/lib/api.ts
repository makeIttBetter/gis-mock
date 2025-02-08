// File: src/lib/api.ts
/**
 * src/lib/api.ts
 * Generic API helper functions for REST calls.
 */

import {API_ENDPOINTS} from "@/config";

/**
 * Extract the JWT token from document.cookie if present.
 */
function getTokenFromCookie(): string | null {
    if (typeof document === "undefined") return null;
    const match = document.cookie.match(/(^|;\s*)jwtToken=([^;]+)/);
    return match ? decodeURIComponent(match[2]) : null;
}

/**
 * Convert an object into a query string.
 * @param paramsObj Object with key-value pairs.
 */
function toQueryString(paramsObj: Record<string, any>): string {
    const searchParams = new URLSearchParams();
    Object.entries(paramsObj).forEach(([key, val]) => {
        if (val !== undefined && val !== null) {
            searchParams.append(key, String(val));
        }
    });
    return searchParams.toString();
}

/**
 * Encode a path string by splitting on '/' so that slashes are preserved.
 * Each segment is individually encoded.
 * @param path The path string.
 */
function encodePath(path: string): string {
    return path
        .split("/")
        .map((segment) => encodeURIComponent(segment))
        .join("/");
}


/**
 * Helper to add the Authorization header if we have a JWT cookie.
 */
function getAuthHeaders(): HeadersInit {
    // const token = getTokenFromCookie();
    // if (token) {
    //     headers["Authorization"] = `Bearer ${token}`;
    // }
    return {
        Accept: "application/json",
    };
}

/**
 * Generic GET request.
 */
export async function apiGet<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    queryParams?: Record<string, any>
): Promise<T> {
    const baseUrl = API_ENDPOINTS[endpoint];
    const url = queryParams ? `${baseUrl}?${toQueryString(queryParams)}` : baseUrl;

    const res = await fetch(url, {
        method: "GET",
        headers: getAuthHeaders(), // <--- attach token if available
        credentials: "include",
    });
    if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.error || `GET ${url} failed: ${res.status}`);
    }
    return res.json() as Promise<T>;
}

/**
 * Construct a URL by appending a path segment to the baseEndpoint,
 * then perform a GET request.
 */
export async function apiGetPath<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    path: string,
    queryParams?: Record<string, any>
): Promise<T> {
    const baseUrl = API_ENDPOINTS[endpoint];
    const encodedPath = encodePath(path);
    const url =
        `${baseUrl}/${encodedPath}` +
        (queryParams ? `?${toQueryString(queryParams)}` : "");

    const res = await fetch(url, {
        method: "GET",
        headers: getAuthHeaders(), // <--- attach token if available
        credentials: "include",
    });
    if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.error || `GET ${url} failed: ${res.status}`);
    }
    return res.json() as Promise<T>;
}

/**
 * Generic POST request with JSON payload.
 */
export async function apiPost<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    data: any
): Promise<T> {
    const url = API_ENDPOINTS[endpoint];
    const res = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            ...getAuthHeaders(), // <--- attach token if available
        },
        credentials: "include",
        body: JSON.stringify(data),
    });
    if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.error || `POST ${url} failed: ${res.status}`);
    }
    return res.json() as Promise<T>;
}

/**
 * Generic POST request to a sub–path of the base endpoint.
 */
export async function apiPostPath<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    path: string,
    data: any,
    queryParams?: Record<string, any>
): Promise<T> {
    const baseUrl = API_ENDPOINTS[endpoint];
    const encodedPath = encodePath(path);
    const url =
        `${baseUrl}/${encodedPath}` +
        (queryParams ? `?${toQueryString(queryParams)}` : "");

    const res = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            ...getAuthHeaders(), // <--- attach token if available
        },
        credentials: "include",
        body: JSON.stringify(data),
    });
    if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.error || `POST ${url} failed: ${res.status}`);
    }
    return res.json() as Promise<T>;
}

/**
 * Generic PUT request.
 */
export async function apiPut<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    id: string,
    data: any
): Promise<T> {
    const url = `${API_ENDPOINTS[endpoint]}/${id}`;
    const res = await fetch(url, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            ...getAuthHeaders(), // <--- attach token if available
        },
        credentials: "include",
        body: JSON.stringify(data),
    });
    if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.error || `PUT ${url} failed: ${res.status}`);
    }
    return res.json() as Promise<T>;
}

/**
 * Generic DELETE request.
 */
export async function apiDelete(
    endpoint: keyof typeof API_ENDPOINTS,
    id: string | number
): Promise<boolean> {
    const url = `${API_ENDPOINTS[endpoint]}/${id}`;
    const res = await fetch(url, {
        method: "DELETE",
        headers: getAuthHeaders(), // <--- attach token if available
        credentials: "include",
    });
    if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.error || `DELETE ${url} failed: ${res.status}`);
    }
    return true;
}

/**
 * Generic POST request for FormData payload.
 */
export async function apiPostFormData<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    path: string,
    formData: FormData
): Promise<T> {
    const baseUrl = API_ENDPOINTS[endpoint];
    const encodedPath = encodePath(path);
    const url = `${baseUrl}/${encodedPath}`;
    const res = await fetch(url, {
        method: "POST",
        // Do NOT set "Content-Type", the browser sets it automatically when sending FormData.
        headers: getAuthHeaders(), // <--- attach token if available
        credentials: "include",
        body: formData,
    });
    if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.error || `POST ${url} failed: ${res.status}`);
    }
    return res.json() as Promise<T>;
}

