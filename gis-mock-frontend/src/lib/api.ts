// src/lib/api.ts
/**
 * Generic API helper functions for REST calls.
 */
import { API_ENDPOINTS } from "@/config";

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
        headers: { Accept: "application/json" },
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
    const url =
        `${baseUrl}/${encodeURIComponent(path)}` +
        (queryParams ? `?${toQueryString(queryParams)}` : "");
    const res = await fetch(url, {
        method: "GET",
        headers: { Accept: "application/json" },
    });
    if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(
            errorData.error || `GET ${url} failed: ${res.status}`
        );
    }
    return res.json() as Promise<T>;
}



/**
 * Generic POST request.
 */
export async function apiPost<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    data: any
): Promise<T> {
    const url = API_ENDPOINTS[endpoint];
    const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
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
        headers: { "Content-Type": "application/json" },
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
    });
    if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.error || `DELETE ${url} failed: ${res.status}`);
    }
    return true;
}
