"use client";
import React from "react";
import CsvUploadForm from "@/components/CsvUploadForm";

export default function CsvUploadPage() {
    return (
        <div className="p-4">
            <h1 className="text-2xl font-bold mb-4">CSV Upload</h1>
            <CsvUploadForm/>
        </div>
    );
}
