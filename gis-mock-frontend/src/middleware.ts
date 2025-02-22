import type { NextRequest } from "next/server";
import { NextResponse } from "next/server";
import { verifyToken } from "@/lib/authApi"; // import your new function

// Debug logging (remove in production)
console.log("Middleware loaded");

// This must match whatever you have in config or environment for your API endpoints.
// But typically, you'll rely on the "verifyToken(cookieHeader)" to do the actual request.
export async function middleware(request: NextRequest) {
    const { pathname } = request.nextUrl;
    console.log("Middleware started processing request for:", pathname);

    // Allow requests to these paths without redirection:
    if (
        pathname.startsWith("/_next") ||    // Next.js internal files
        pathname.startsWith("/favicon.ico") ||
        pathname.startsWith("/login") ||
        pathname.startsWith("/api/auth")    // if your Next routes for auth are public
    ) {
        return NextResponse.next();
    }

    // Grab the cookie header from the incoming request
    const cookieHeader = request.headers.get("cookie") || "";

    // We can do a quick check if the "jwtToken" is present at all,
    // but let's rely on verifyToken() which returns false if invalid or missing.
    try {
        const isValid = await verifyToken(cookieHeader);

        if (isValid) {
            console.log("JWT token verified successfully, continuing request.");
            return NextResponse.next();
        } else {
            // If not valid, redirect to /login
            console.log("JWT token is invalid or expired. Redirecting to /login");
            const loginUrl = request.nextUrl.clone();
            loginUrl.pathname = "/login";
            return NextResponse.redirect(loginUrl);
        }
    } catch (error) {
        console.error("Error verifying token:", error);
        // In case of error, also redirect to /login
        const loginUrl = request.nextUrl.clone();
        loginUrl.pathname = "/login";
        return NextResponse.redirect(loginUrl);
    }
}

/**
 * This matcher tells Next.js to run middleware on every route except:
 * - files with extensions (.css, .png, .jpg, etc.)
 * - the /_next folder
 */
export const config = {
    matcher: ["/((?!.*\\..*|_next).*)"],
};
