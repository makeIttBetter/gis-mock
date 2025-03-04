// src/app/(site)/login/page.tsx
"use client";

import React, {useState} from "react";
import {useRouter} from "next/navigation";

export default function LoginPage() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const router = useRouter();

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError(null);

        try {
            // Call your backend sign-in endpoint.
            // Ensure NEXT_PUBLIC_BACKEND_URL is set correctly (e.g., "http://localhost:8080")
            const response = await fetch(
                `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/auth/signin`,
                {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({username, password}),
                }
            );

            console.log("response", response);

            if (!response.ok) {
                console.error("Sign-in failed:", response);
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.message || "Sign-in failed.");
            }

            const data = await response.json();
            if (!data.token) {
                console.error("No token returned from server:", data);
                throw new Error("No token returned from server");
            }

            // ALTERNATIVE Bearer token logic: Store token in localStorage
            // localStorage.setItem("jwtToken", data.token);

            // ORIGINAL HTTPOnly cookie logic (for reference):
            const maxAge = 24 * 60 * 60; // 1 day in seconds
            document.cookie = `jwtToken=${data.token}; Path=/; Max-Age=${maxAge}; SameSite=Strict;`;

            console.log("Sign-in successful! Redirecting to /map...");
            // Redirect to the homepage (or any other protected route)
            router.push("/map");
        } catch (err: any) {
            setError(err.message);
        }
    }

    return (
        <div className="flex items-center justify-center min-h-screen bg-gray-100">
            <form
                onSubmit={handleSubmit}
                className="bg-white p-6 rounded shadow-md w-full max-w-sm"
            >
                <h1 className="text-2xl font-bold mb-4">Login</h1>
                {error && <div className="text-red-600 mb-3">{error}</div>}
                <div className="mb-3">
                    <label className="block mb-1 text-sm font-medium">Username</label>
                    <input
                        type="text"
                        className="border rounded w-full p-2"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />
                </div>
                <div className="mb-4">
                    <label className="block mb-1 text-sm font-medium">Password</label>
                    <input
                        type="password"
                        className="border rounded w-full p-2"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>
                <button
                    type="submit"
                    className="bg-blue-600 text-white px-4 py-2 rounded w-full"
                >
                    Sign In
                </button>
            </form>
        </div>
    );
}
