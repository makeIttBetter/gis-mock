"use client";

import React, {useState} from "react";
import {useRouter} from "next/navigation";

/**
 * Makes the sign-in call to the backend.
 * The backend now sets an **HttpOnly / Secure / SameSite=None** cookie,
 * so the frontend no longer touches `document.cookie`.
 */
async function signInRequest(username: string, password: string): Promise<void> {
    const res = await fetch(
        `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/auth/signin`,
        {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            credentials: "include", // <- IMPORTANT: forward cookies both ways
            body: JSON.stringify({username, password}),
        }
    );

    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || "Sign-in failed");
    }
}

export default function LoginPage() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const router = useRouter();

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError(null);

        try {
            await signInRequest(username, password); // one call is enough
            // Refresh the page so that Next.js middleware picks up the newly set cookie
            router.refresh();
            // Second sign-in attempt to ensure the token is properly recognized
            await signInRequest(username, password);

            console.log("Sign-in successful! Redirecting to /map...");
            router.push("/map");                    // backend cookie is already set
        } catch (err: any) {
            setError(err.message);
        }
    }

    return (
        <div className="flex min-h-screen items-center justify-center bg-gray-100">
            <form
                onSubmit={handleSubmit}
                className="w-full max-w-sm rounded bg-white p-6 shadow-md"
            >
                <h1 className="mb-4 text-2xl font-bold">Login</h1>

                {error && <p className="mb-3 text-red-600">{error}</p>}

                <div className="mb-3">
                    <label className="mb-1 block text-sm font-medium">Username</label>
                    <input
                        type="text"
                        className="w-full rounded border p-2"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />
                </div>

                <div className="mb-4">
                    <label className="mb-1 block text-sm font-medium">Password</label>
                    <input
                        type="password"
                        className="w-full rounded border p-2"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>

                <button
                    type="submit"
                    className="w-full rounded bg-blue-600 px-4 py-2 text-white"
                >
                    Sign In
                </button>
            </form>
        </div>
    );
}
