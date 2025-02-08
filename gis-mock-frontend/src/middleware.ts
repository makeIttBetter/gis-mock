// File: middleware.ts
import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

// Debug logging: (remove or comment out in production)
console.log("Middleware loaded");

export function middleware(request: NextRequest) {
    const { pathname } = request.nextUrl;

    console.log("Middleware started processing request for:", pathname);
    // Allow requests to these paths without redirection:
    if (
        pathname.startsWith("/_next") ||         // Next.js internal files
        pathname.startsWith("/favicon.ico") ||     // favicon
        pathname.startsWith("/login") ||           // the login page itself
        pathname.startsWith("/api/auth")           // any public auth API routes (if needed)
    ) {
        return NextResponse.next();
    }

    console.log("Middleware processing request for:", pathname);

    // Check if the jwtToken cookie exists
    const token = request.cookies.get("jwtToken")?.value;
    if (!token) {
        // Redirect to /login if not logged in
        const loginUrl = request.nextUrl.clone();
        loginUrl.pathname = "/login";
        return NextResponse.redirect(loginUrl);
    }

    console.log("Token found, proceeding with request:", token);

    // If token exists, continue with the request
    return NextResponse.next();
}

/**
 * This matcher tells Next.js to run middleware on every route except:
 *   - files with extensions (like .css, .png, etc.)
 *   - the /_next folder
 */
export const config = {
    matcher: ["/((?!.*\\..*|_next).*)"],
};
