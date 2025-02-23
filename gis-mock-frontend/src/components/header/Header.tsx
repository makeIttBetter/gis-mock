"use client";
import React from "react";
import Link from "next/link";

const Header: React.FC = () => {
    return (
        <header className="w-full bg-white shadow-md fixed top-0 left-0 z-50">
            <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-around">
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
                {/* NEW: Profile link */}
                <Link
                    href="/profile"
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
                >
                    Profile
                </Link>
            </div>
        </header>
    );
};

export default Header;
