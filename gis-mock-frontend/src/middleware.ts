// src/app/middleware.ts
import type {NextRequest} from "next/server";
import {NextResponse} from "next/server";

// Debug logging: (remove or comment out in production)
console.log("Middleware loaded");

export function middleware(request: NextRequest) {
    const {pathname} = request.nextUrl;

    console.log("Middleware started processing request for:", pathname);
    // Allow requests to these paths without redirection:
    if (
        pathname.startsWith("/_next") ||       // Next.js internal files
        pathname.startsWith("/favicon.ico") ||   // favicon
        pathname.startsWith("/login") ||         // the login page itself
        pathname.startsWith("/api/auth")         // any public auth API routes (if needed)
    ) {
        return NextResponse.next();
    }

    // ALTERNATIVE Bearer token logic: Check the Authorization header
    // const authHeader = request.headers.get("authorization");
    // const token = localStorage.getItem("jwtToken");
    //
    // if (token == null || !token) {
    //     const loginUrl = request.nextUrl.clone();
    //     loginUrl.pathname = "/login";
    //     return NextResponse.redirect(loginUrl);
    // }
    //
    // console.log("Bearer token found, proceeding with request");
    // return NextResponse.next();

    // ORIGINAL HTTPOnly cookie logic (for reference):
    const token = request.cookies.get("jwtToken")?.value;
    if (!token) {
        const loginUrl = request.nextUrl.clone();
        loginUrl.pathname = "/login";
        return NextResponse.redirect(loginUrl);
    }
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
