"use client";
import React, { useState } from "react";
import { uploadCsvFile } from "@/lib/realEstateCsvApi";

interface CsvError {
    rowNumber: number;
    mlsNumber?: string;
    address?: string;
    errorMessage: string;
}

interface UploadResult {
    totalRows: number;
    processedRows: number;
    errors: CsvError[];
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
            const result = await uploadCsvFile(file);
            setUploadResult(result);
        } catch (error) {
            console.error("Upload error:", error);
            alert("File upload failed.");
        } finally {
            setLoading(false);
        }
    };

    const handleCopy = (text: string) => {
        navigator.clipboard.writeText(text);
        alert("Copied to clipboard!");
    };

    return (
        <div className="bg-white p-4 rounded shadow">
            <form onSubmit={handleUpload}>
                <div className="mb-4">
                    <label className="block text-sm font-medium">Select CSV File:</label>
                    <input type="file" accept=".csv" onChange={handleFileChange} className="mt-1" />
                </div>
                <button type="submit" className="px-4 py-2 bg-blue-600 text-white rounded" disabled={loading}>
                    {loading ? "Uploading..." : "Upload"}
                </button>
            </form>
            {uploadResult && (
                <div className="mt-4">
                    <h2 className="text-xl font-semibold">Upload Summary</h2>
                    <p>Total Rows: {uploadResult.totalRows}</p>
                    <p>Processed Rows: {uploadResult.processedRows}</p>
                    {uploadResult.errors.length > 0 ? (
                        <div className="mt-4">
                            <h3 className="text-lg font-semibold">Errors:</h3>
                            <table className="min-w-full divide-y divide-gray-200 mt-2">
                                <thead>
                                <tr>
                                    <th className="px-4 py-2">Row</th>
                                    <th className="px-4 py-2">MLS#</th>
                                    <th className="px-4 py-2">Address</th>
                                    <th className="px-4 py-2">Error</th>
                                    <th className="px-4 py-2">Copy</th>
                                </tr>
                                </thead>
                                <tbody>
                                {uploadResult.errors.map((error, idx) => (
                                    <tr key={idx} className="border-t">
                                        <td className="px-4 py-2">{error.rowNumber}</td>
                                        <td className="px-4 py-2">{error.mlsNumber}</td>
                                        <td className="px-4 py-2">{error.address}</td>
                                        <td className="px-4 py-2">{error.errorMessage}</td>
                                        <td className="px-4 py-2">
                                            <button
                                                onClick={() =>
                                                    handleCopy(
                                                        `MLS#: ${error.mlsNumber || ""}, Address: ${error.address || ""}`
                                                    )
                                                }
                                                className="px-2 py-1 bg-green-500 text-white rounded"
                                            >
                                                Copy
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    ) : (
                        <p className="mt-2 text-green-600">File processed successfully with no errors.</p>
                    )}
                </div>
            )}
        </div>
    );
};

export default CsvUploadForm;
