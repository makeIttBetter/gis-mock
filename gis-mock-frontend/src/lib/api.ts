/**
 * Central REST helper.
 * Now every error looks for either `message` or `error`
 * coming back from the backend before falling back to status text.
 */
import { API_ENDPOINTS } from "@/config";

function toQueryString(obj: Record<string, any>): string {
    const s = new URLSearchParams();
    Object.entries(obj).forEach(([k, v]) => {
        if (v !== undefined && v !== null) s.append(k, String(v));
    });
    return s.toString();
}

function encodePath(path: string): string {
    return path.split("/").map(encodeURIComponent).join("/");
}

/* -------------------------------------------------- */
/*  unified helper for all verbs                      */
/* -------------------------------------------------- */
async function handleError(res: Response, url: string): Promise<never> {
    const errData = await res.json().catch(() => ({}));
    const msg =
        errData.message ||
        errData.error ||
        `${res.status} ${res.statusText} (${url})`;
    throw new Error(msg);
}

/* -------------------------------------------------- */
/*  GET                                               */
/* -------------------------------------------------- */
export async function apiGet<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    queryParams?: Record<string, any>,
    headers?: Record<string, string>,
): Promise<T> {
    const base = API_ENDPOINTS[endpoint];
    const url = queryParams ? `${base}?${toQueryString(queryParams)}` : base;

    const res = await fetch(url, {
        method: "GET",
        headers: { Accept: "application/json", ...(headers || {}) },
        credentials: "include",
    });
    if (!res.ok) return handleError(res, url);
    return res.json() as Promise<T>;
}

/* -------------------------------------------------- */
/*  GET with extra path                               */
/* -------------------------------------------------- */
export async function apiGetPath<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    path: string,
    queryParams?: Record<string, any>,
): Promise<T> {
    const url =
        `${API_ENDPOINTS[endpoint]}/${encodePath(path)}` +
        (queryParams ? `?${toQueryString(queryParams)}` : "");

    const res = await fetch(url, {
        method: "GET",
        headers: { Accept: "application/json" },
        credentials: "include",
    });
    if (!res.ok) return handleError(res, url);
    return res.json() as Promise<T>;
}

/* -------------------------------------------------- */
/*  POST (JSON)                                       */
/* -------------------------------------------------- */
export async function apiPost<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    data: any,
): Promise<T> {
    const url = API_ENDPOINTS[endpoint];
    const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(data),
    });
    if (!res.ok) return handleError(res, url);
    return res.json() as Promise<T>;
}

/* -------------------------------------------------- */
/*  POST to sub-path (JSON)                           */
/* -------------------------------------------------- */
export async function apiPostPath<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    path: string,
    data: any,
    queryParams?: Record<string, any>,
): Promise<T> {
    const url =
        `${API_ENDPOINTS[endpoint]}/${encodePath(path)}` +
        (queryParams ? `?${toQueryString(queryParams)}` : "");

    const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(data),
    });
    if (!res.ok) return handleError(res, url);
    return res.json() as Promise<T>;
}

/* -------------------------------------------------- */
/*  PUT                                               */
/* -------------------------------------------------- */
export async function apiPut<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    id: string,
    data: any,
): Promise<T> {
    const url = `${API_ENDPOINTS[endpoint]}/${id}`;
    const res = await fetch(url, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(data),
    });
    if (!res.ok) return handleError(res, url);
    return res.json() as Promise<T>;
}

/* -------------------------------------------------- */
/*  DELETE (id)                                       */
/* -------------------------------------------------- */
export async function apiDelete(
    endpoint: keyof typeof API_ENDPOINTS,
    id: string | number,
): Promise<boolean> {
    const url = `${API_ENDPOINTS[endpoint]}/${id}`;
    const res = await fetch(url, {
        method: "DELETE",
        credentials: "include",
    });
    if (!res.ok) return handleError(res, url);
    return true;
}

/* -------------------------------------------------- */
/*  POST (FormData)                                   */
/* -------------------------------------------------- */
export async function apiPostFormData<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    path: string,
    formData: FormData,
): Promise<T> {
    const url = `${API_ENDPOINTS[endpoint]}/${encodePath(path)}`;
    const res = await fetch(url, {
        method: "POST",
        credentials: "include",
        body: formData,               // browser sets multipart headers
    });
    if (!res.ok) return handleError(res, url);
    return res.json() as Promise<T>;
}

/* -------------------------------------------------- */
/*  Helper: DELETE to sub-path                        */
/* -------------------------------------------------- */
export async function apiDeletePath(
    endpoint: keyof typeof API_ENDPOINTS,
    path: string,
): Promise<void> {
    const url = `${API_ENDPOINTS[endpoint]}/${encodePath(path)}`;
    const res = await fetch(url, {
        method: "DELETE",
        credentials: "include",
    });
    if (!res.ok) return handleError(res, url);
}
