import type {NextRequest} from "next/server";
import {NextResponse} from "next/server";
import {verifyToken} from "@/lib/authApi"; // import your new function

// Debug logging (remove in production)
console.log("Middleware loaded");

// This must match whatever you have in config or environment for your API endpoints.
// But typically, you'll rely on the "verifyToken(cookieHeader)" to do the actual request.
export async function middleware(request: NextRequest) {
    const {pathname} = request.nextUrl;
    console.log("Middleware processing:", pathname);

    // 1) Skip auth checks for home page "/", plus these other paths:
    if (
        pathname === "/" ||
        pathname.startsWith("/_next") ||
        pathname.startsWith("/favicon.ico") ||
        pathname.startsWith("/login") ||
        pathname.startsWith("/api")
    ) {
        console.log("Skipping token check for:", pathname);
        return NextResponse.next();
    }

    // 2) Otherwise, verify the token
    const cookieHeader = request.headers.get("cookie") || "";
    console.log("Cookie header:", cookieHeader);

    try {
        const isValid = await verifyToken(cookieHeader);
        console.log("Token validity:", isValid);

        if (isValid) {
            console.log("Token valid, proceeding.");
            return NextResponse.next();
        } else {
            console.log("Token invalid, redirecting to /login");
            const loginUrl = request.nextUrl.clone();
            loginUrl.pathname = "/login";
            return NextResponse.redirect(loginUrl);
        }
    } catch (error) {
        console.error("Token verification error:", error);
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
