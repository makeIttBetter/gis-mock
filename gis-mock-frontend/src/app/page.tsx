"use client";
import React from "react";
import Link from "next/link";

export default function Home() {
    return (
        <main className="flex flex-col items-center min-h-screen p-8 bg-gray-100">
            {/* Title */}
            <h1 className="text-4xl sm:text-6xl font-bold mb-8 text-center">
                Real Estate Management Tool
            </h1>

            {/* Simple menu with the same links as the header */}
            <nav className="flex flex-col sm:flex-row gap-4">
                <Link
                    href="/"
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
                >
                    Home
                </Link>
                <Link
                    href="/map"
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
                >
                    Map
                </Link>
                <Link
                    href="/upload-csv"
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
                >
                    Upload CSV
                </Link>
                <Link
                    href="/profile"
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
                >
                    Profile
                </Link>
            </nav>
        </main>
    );
}
