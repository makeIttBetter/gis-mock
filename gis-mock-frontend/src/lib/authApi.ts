/**
 * src/lib/authApi.ts
 * Provides authentication-related API calls,
 * including verifying the JWT token via backend.
 */

// import {API_ENDPOINTS} from "@/config";

/**
 * The response shape from /api/auth/verify
 */
interface TokenVerificationResponse {
    valid: boolean;
    message: string;
}

/**
 * Verify the JWT token by calling the backend's /api/auth/verify endpoint.
 * We must pass the user's cookies in the `cookie` header so the server
 * can find the HttpOnly jwtToken cookie.
 * If it's valid, return true. Otherwise throw an Error or return false.
 */
export async function verifyToken(cookieHeader: string): Promise<boolean> {
    console.log("Verifying token...");
    // Build the URL from your config
    // const url = `${API_ENDPOINTS.AUTH_VERIFY}`;
    const url = `${process.env.INTERNAL_BACKEND_URL}/api/auth/verify`; // Use internal URL

    console.log("Verifying token at:", url);

    // Because the Next.js middleware runs on the server side,
    // we can pass the `cookieHeader` from the incoming request
    // to the backend, thus including the 'jwtToken' HttpOnly cookie.
    const res = await fetch(url, {
        method: "GET",
        headers: {
            Cookie: cookieHeader,
        },
        // 'credentials: include' not strictly needed on the server,
        // but included for consistency.
        // credentials: "include",
    });

    console.log("Token verification response:", res);

    if (!res.ok) {
        // e.g. 401 means invalid or missing token
        return false;
    }

    // If 200, parse JSON
    const data = (await res.json()) as TokenVerificationResponse;
    return data.valid;
}

/**
 * Optionally, if you'd rather unify everything in your "api.ts",
 * you can do a specialized call like:
 */
// export async function verifyTokenViaApiGet(cookieHeader: string): Promise<boolean> {
//   // We can't pass custom headers easily to apiGet, so we'd need a specialized function.
//   // This is just an example if you wanted to keep the same style as "apiGet" calls.
//   // For the middleware approach, the fetch version above is simpler.
//   return false;
// }
