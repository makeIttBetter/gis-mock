// frontend/src/components/Header.tsx

"use client";
import React from "react";
import Link from "next/link";
import Image from "next/image";

const Header: React.FC = () => {
    // Developer note: This header is displayed on all pages.
    // We use a simple fixed position with a white background
    // and a shadow effect for clarity.
    return (
        <header className="w-full bg-white shadow-md fixed top-0 left-0 z-50">
            <div className="max-w-7xl mx-auto px-4 py-3 flex items-center">
                <Link href="/" className="flex items-center">
                    <Image
                        src="/logo.png"
                        alt="Financial Data Filtering App Logo"
                        width={50}
                        height={50}
                        className="cursor-pointer"
                    />
                    <span className="ml-2 text-xl font-semibold text-gray-800">
            Financial Data Filtering App
          </span>
                </Link>
            </div>
        </header>
    );
};

export default Header;
