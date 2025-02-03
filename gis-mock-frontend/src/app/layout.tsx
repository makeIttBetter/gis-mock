// frontend/src/app/layout.tsx

"use client";
import React from "react";
import Header from "@/components/Header";
import "./globals.css";

interface RootLayoutProps {
    children: React.ReactNode;
}

const RootLayout: React.FC<RootLayoutProps> = ({ children }) => {
    return (
        <html lang="en">
        <head>
            <title>Financial Data Filtering App</title>
        </head>
        <body className="bg-gray-100">
        {/* Header appears on all pages */}
        <Header />
        {/* Main content */}
        <main className="pt-16">{children}</main>
        </body>
        </html>
    );
};

export default RootLayout;
