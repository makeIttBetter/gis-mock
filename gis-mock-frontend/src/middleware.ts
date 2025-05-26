import type {NextRequest} from "next/server";
import {NextResponse} from "next/server";
import {verifyToken} from "@/lib/authApi";

// Debug logging (remove in production)
console.log("Middleware loaded");

export async function middleware(request: NextRequest) {
    const {pathname} = request.nextUrl;
    console.log("Middleware processing:", pathname);

    // 1) Skip auth checks for specific paths
    if (
        pathname === "/" ||
        pathname.startsWith("/_next") ||
        pathname.startsWith("/favicon.ico") ||
        pathname.startsWith("/login") ||
        pathname.startsWith("/api")
    ) {
        console.log("Skipping token check for:", pathname);

        // Make sure we remove any unwanted headers, just in case
        const response = NextResponse.next();
        response.headers.delete("WWW-Authenticate");
        return response;
    }

    // 2) Otherwise, verify the token
    const cookieHeader = request.headers.get("cookie") || "";
    console.log("Cookie header:", cookieHeader);

    try {
        const isValid = await verifyToken(cookieHeader);
        console.log("Token validity:", isValid);

        if (isValid) {
            console.log("Token valid, proceeding.");
            const response = NextResponse.next();
            // Remove any accidental `WWW-Authenticate` header just in case
            response.headers.delete("WWW-Authenticate");
            return response;
        } else {
            console.log("Token invalid, redirecting to /login");
            const loginUrl = request.nextUrl.clone();
            loginUrl.pathname = "/login";

            // Return redirect, but remove `WWW-Authenticate` so no popup
            const redirectRes = NextResponse.redirect(loginUrl);
            redirectRes.headers.delete("WWW-Authenticate");
            return redirectRes;
        }
    } catch (error) {
        console.error("Token verification error:", error);
        const loginUrl = request.nextUrl.clone();
        loginUrl.pathname = "/login";

        // Return redirect, but remove `WWW-Authenticate` so no popup
        const redirectRes = NextResponse.redirect(loginUrl);
        redirectRes.headers.delete("WWW-Authenticate");
        return redirectRes;
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
