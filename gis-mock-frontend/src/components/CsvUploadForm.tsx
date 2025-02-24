// File: src/components/CsvUploadForm.tsx
"use client";
import React, {useState} from "react";
import {uploadCsvFile} from "@/lib/realEstateCsvApi";

interface CsvError {
    rowNumber: number;
    mlsNumber?: string;
    address?: string;
    errorMessage: string;
}

interface UploadResult {
    totalRows: number;
    processedRows: number;
    updatedCount: number;
    createdCount: number;
    errors: CsvError[];

    headerRow?: string[];
    unsavedRows?: string[][];
}

const CsvUploadForm: React.FC = () => {
    const [file, setFile] = useState<File | null>(null);
    const [uploadResult, setUploadResult] = useState<UploadResult | null>(null);
    const [loading, setLoading] = useState<boolean>(false);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            setFile(e.target.files[0]);
        }
    };

    const handleUpload = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!file) return;
        setLoading(true);
        try {
            const result = await uploadCsvFile(file);  // your existing call
            setUploadResult(result);
        } catch (error: any) {
            console.error("Upload error:", error);
            alert("File upload failed: " + error?.message || "");
        } finally {
            setLoading(false);
        }
    };


    /**
     * Generates a client-side CSV download for the not-saved rows.
     */
    const handleDownloadNotSavedRows = () => {
        if (!uploadResult || !uploadResult.headerRow || !uploadResult.unsavedRows) return;

        // Create CSV lines in memory
        const lines: string[] = [];

        // Rebuild the header line
        const headerLine = uploadResult.headerRow.map(cell => escapeCsv(cell)).join(",");
        lines.push(headerLine);

        // Add each unsaved row
        for (const rowArr of uploadResult.unsavedRows) {
            const rowLine = rowArr.map(cell => escapeCsv(cell)).join(",");
            lines.push(rowLine);
        }

        // Join with newlines to make final CSV text
        const csvContent = lines.join("\n");

        // Trigger download
        const blob = new Blob([csvContent], {type: "text/csv;charset=utf-8;"});
        const url = URL.createObjectURL(blob);

        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", "not-saved-rows.csv");
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    // Simple CSV escaping
    function escapeCsv(value: string) {
        if (!value) return "";
        // If it contains a comma or newline, wrap in quotes and escape internal quotes
        const mustQuote = value.includes(",") || value.includes("\n");
        const escaped = value.replace(/"/g, '""');
        return mustQuote ? `"${escaped}"` : escaped;
    }

    return (
        <div className="bg-white p-4 rounded shadow">
            <form onSubmit={handleUpload}>
                <div className="mb-4">
                    <label className="block text-sm font-medium">Select CSV File:</label>
                    <input
                        type="file"
                        accept=".csv"
                        onChange={handleFileChange}
                        className="mt-1"
                    />
                </div>
                <button
                    type="submit"
                    className="px-4 py-2 bg-blue-600 text-white rounded"
                    disabled={loading}
                >
                    {loading ? "Uploading..." : "Upload"}
                </button>
            </form>

            {uploadResult && (
                <div className="mt-4">
                    <h2 className="text-xl font-semibold">Upload Summary</h2>
                    <p>Total Rows: {uploadResult.totalRows}</p>
                    <p>Processed Rows: {uploadResult.processedRows}</p>
                    <p>Updated Rows: {uploadResult.updatedCount}</p>
                    <p>New Rows: {uploadResult.createdCount}</p>

                    {uploadResult.unsavedRows && uploadResult.unsavedRows.length > 0 && (
                        <div className="mt-2">
                            <p className="text-red-600">
                                {uploadResult.unsavedRows.length} rows were NOT saved.
                            </p>
                            <button
                                onClick={handleDownloadNotSavedRows}
                                className="px-3 py-1 bg-orange-500 text-white rounded"
                            >
                                Download Not-Saved Rows
                            </button>
                        </div>
                    )}

                    {uploadResult.errors.length > 0 ? (
                        <div className="mt-4">
                            <h3 className="text-lg font-semibold mb-2">Errors / Skipped Rows:</h3>
                            <table className="min-w-full border border-gray-300">
                                <thead className="bg-gray-50">
                                <tr>
                                    <th className="border-b border-gray-300 px-4 py-2 text-center">
                                        Row
                                    </th>
                                    <th className="border-b border-gray-300 px-4 py-2 text-center">
                                        MLS#
                                    </th>
                                    <th className="border-b border-gray-300 px-4 py-2 text-center">
                                        Address
                                    </th>
                                    <th className="border-b border-gray-300 px-4 py-2 text-center">
                                        Reason
                                    </th>
                                </tr>
                                </thead>
                                <tbody>
                                {uploadResult.errors.map((error, idx) => (
                                    <tr key={idx} className="border-b border-gray-200 text-center">
                                        <td className="px-4 py-2">{error.rowNumber}</td>
                                        <td className="px-4 py-2">{error.mlsNumber}</td>
                                        <td className="px-4 py-2">{error.address}</td>
                                        <td className="px-4 py-2">{error.errorMessage}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    ) : (
                        <p className="mt-2 text-green-600">
                            File processed successfully with no errors.
                        </p>
                    )}
                </div>
            )}
        </div>
    );
};

export default CsvUploadForm;
