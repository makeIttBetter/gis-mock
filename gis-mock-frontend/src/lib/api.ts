// frontend/src/lib/api.ts

import { API_ENDPOINTS } from "@/config";

/*
  Developer note: Helper to convert an object into a query string.
  Example: { foo: "bar", page: 2 } => "foo=bar&page=2"
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

/*
  Generic GET request to any endpoint from API_ENDPOINTS.
  We handle common issues like error responses here.
*/
export async function apiGet<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    queryParams?: Record<string, any>
): Promise<T> {
  const baseUrl = API_ENDPOINTS[endpoint];
  const url = queryParams ? `${baseUrl}?${toQueryString(queryParams)}` : baseUrl;

  const res = await fetch(url, {
    method: "GET",
    // credentials: "include", // Adjust if you don't need cookies/auth tokens
    headers: { Accept: "application/json" },
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.error || `GET ${url} failed: ${res.status}`);
  }
  return res.json() as Promise<T>;
}

/*
  Generic POST request for creating data in our API.
*/
export async function apiPost<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    data: any
): Promise<T> {
  const url = API_ENDPOINTS[endpoint];

  const res = await fetch(url, {
    method: "POST",
    // credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.error || `POST ${url} failed: ${res.status}`);
  }
  return res.json().catch(() => ({})) as Promise<T>;
}

/*
  Generic PUT request to update data by ID.
*/
export async function apiPut<T>(
    endpoint: keyof typeof API_ENDPOINTS,
    id: string | number,
    data: any
): Promise<T> {
  const url = `${API_ENDPOINTS[endpoint]}/${id}`;

  const res = await fetch(url, {
    method: "PUT",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.error || `PUT ${url} failed: ${res.status}`);
  }
  return res.json().catch(() => ({})) as Promise<T>;
}

/*
  Generic DELETE request to remove data by ID.
*/
export async function apiDelete(
    endpoint: keyof typeof API_ENDPOINTS,
    id: string | number
): Promise<boolean> {
  const url = `${API_ENDPOINTS[endpoint]}/${id}`;

  const res = await fetch(url, {
    method: "DELETE",
    credentials: "include",
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.error || `DELETE ${url} failed: ${res.status}`);
  }
  return true;
}
