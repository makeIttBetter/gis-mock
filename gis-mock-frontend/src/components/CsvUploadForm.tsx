"use client";
import React, {useEffect, useRef, useState} from "react";
import {
    abortCsvProcessing,
    getCsvUploadStatus,
    RealEstateCsvProcessingStatusDto,
    uploadCsvFile,
} from "@/lib/realEstateCsvApi";

/**
 * A functional component that allows the user to:
 * - Select a file
 * - Upload it
 * - See real-time progress (rows processed out of total)
 * - Possibly abort (cancel) the upload mid-process
 * - See final results
 */
const CsvUploadForm: React.FC = () => {
    const [file, setFile] = useState<File | null>(null);
    const [status, setStatus] = useState<RealEstateCsvProcessingStatusDto | null>(null);
    const [loading, setLoading] = useState<boolean>(false);

    // We'll store a ref to the interval timer for polling
    const pollingIntervalRef = useRef<NodeJS.Timeout | null>(null);

    /**
     * Called when user selects a file from <input type="file" />
     */
    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            setFile(e.target.files[0]);
        }
    };

    /**
     * Called when user clicks "Upload".
     */
    const handleUpload = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!file) return;

        setLoading(true);
        try {
            // Start the upload (async processing on the backend).
            await uploadCsvFile(file);

            // Because we know a task just started,
            // we set an initial status with inProgress: true
            // then start polling
            fetchStatus();
            startPolling();
        } catch (error: any) {
            console.error("Upload error:", error);
            alert("File upload failed: " + (error?.message || ""));
        } finally {
            setLoading(false);
        }
    };

    /**
     * Immediately fetches status from the backend and updates our local state.
     */
    const fetchStatus = async () => {
        try {
            const data = await getCsvUploadStatus();
            setStatus(data);

            if (data.inProgress) {
                // If an active task is in progress, continue polling
                startPolling();
            } else {
                // If no active task, stop polling
                stopPolling();
            }
        } catch (err) {
            console.error("Failed to fetch status:", err);
            stopPolling();
        }
    };

    /**
     * Starts polling the status endpoint every 5 seconds (unless it's already running).
     */
    const startPolling = () => {
        if (pollingIntervalRef.current) {
            return; // already polling
        }
        pollingIntervalRef.current = setInterval(fetchStatus, 5000);
    };

    /**
     * Stops polling (clear the interval).
     */
    const stopPolling = () => {
        if (pollingIntervalRef.current) {
            clearInterval(pollingIntervalRef.current);
            pollingIntervalRef.current = null;
        }
    };

    /**
     * On mount (and on refresh), do an initial check for any active tasks.
     */
    useEffect(() => {
        fetchStatus(); // Checks if a task is in progress
        return () => {
            // On unmount, stop polling
            stopPolling();
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    /**
     * Attempt to abort (cancel) the current processing.
     */
    const handleAbort = async () => {
        try {
            await abortCsvProcessing();
            // Immediately fetch status to see if it was aborted
            fetchStatus();
        } catch (err) {
            console.error("Failed to abort CSV:", err);
            alert("Failed to abort CSV: " + (err as Error).message);
        }
    };

    /**
     * Helper to download unsaved rows as CSV, if available.
     */
    const handleDownloadNotSavedRows = () => {
        if (!status?.result?.headerRow || !status.result?.unsavedRows) return;

        const lines: string[] = [];
        const headerLine = status.result.headerRow.map(escapeCsv).join(",");
        lines.push(headerLine);

        for (const row of status.result.unsavedRows) {
            const rowLine = row.map(escapeCsv).join(",");
            lines.push(rowLine);
        }

        const csvContent = lines.join("\n");
        const blob = new Blob([csvContent], {type: "text/csv;charset=utf-8;"});
        const url = URL.createObjectURL(blob);

        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", "not-saved-rows.csv");
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    /**
     * Basic CSV escaping function.
     */
    function escapeCsv(value: string) {
        if (!value) return "";
        const mustQuote = value.includes(",") || value.includes("\n");
        const escaped = value.replace(/"/g, '""');
        return mustQuote ? `"${escaped}"` : escaped;
    }

    // Decide if user can upload:
    // - no status,
    // - or status.inProgress is false,
    // - or status.success is false and no errorMessage
    const canUpload =
        !status ||
        (!status.inProgress && !status.success && !status.errorMessage);

    // For the progress bar
    const totalRows = status?.result?.totalRows ?? 0;
    const processedRows = status?.result?.processedRows ?? 0;
    const percentage = totalRows > 0 ? Math.floor((processedRows / totalRows) * 100) : 0;

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
                        disabled={!canUpload}
                    />
                </div>
                <button
                    type="submit"
                    className="px-4 py-2 bg-blue-600 text-white rounded"
                    disabled={loading || !file || !canUpload}
                >
                    {loading ? "Uploading..." : "Upload"}
                </button>

                {/* Show ABORT button if a file is in progress */}
                {status?.inProgress && (
                    <button
                        type="button"
                        onClick={handleAbort}
                        className="px-4 py-2 bg-red-600 text-white rounded ml-2"
                    >
                        Abort
                    </button>
                )}
            </form>

            {/* Status display if we have a status object */}
            {status && (
                <div className="mt-4">
                    {/* If still in progress, show real-time progress */}
                    {status.inProgress && status.result && (
                        <div className="mb-2">
                            <p>
                                Processing... {status.result.processedRows} of {status.result.totalRows} rows complete.
                            </p>
                            {totalRows > 0 && (
                                <div className="w-full bg-gray-200 rounded-full h-4 mt-2">
                                    <div
                                        className="bg-blue-500 h-4 rounded-full"
                                        style={{width: `${percentage}%`}}
                                    />
                                </div>
                            )}
                        </div>
                    )}

                    {/* If completed successfully, display the result */}
                    {status.success && status.result && (
                        <div className="mt-4">
                            <h2 className="text-xl font-semibold">Upload Summary</h2>
                            <p>Total Rows: {status.result.totalRows}</p>
                            <p>Processed Rows: {status.result.processedRows}</p>
                            <p>Updated Rows: {status.result.updatedCount}</p>
                            <p>New Rows: {status.result.createdCount}</p>

                            {status.result.unsavedRows && status.result.unsavedRows.length > 0 && (
                                <div className="mt-2">
                                    <p className="text-red-600">
                                        {status.result.unsavedRows.length} rows were NOT saved.
                                    </p>
                                    <button
                                        onClick={handleDownloadNotSavedRows}
                                        className="px-3 py-1 bg-orange-500 text-white rounded"
                                    >
                                        Download Not-Saved Rows
                                    </button>
                                </div>
                            )}

                            {status.result.errors.length > 0 && (
                                <div className="mt-4">
                                    <h3 className="text-lg font-semibold mb-2">Errors / Skipped Rows:</h3>
                                    <table className="min-w-full border border-gray-300">
                                        <thead className="bg-gray-50">
                                        <tr>
                                            <th className="border-b border-gray-300 px-4 py-2 text-center">Row</th>
                                            <th className="border-b border-gray-300 px-4 py-2 text-center">MLS#</th>
                                            <th className="border-b border-gray-300 px-4 py-2 text-center">Address</th>
                                            <th className="border-b border-gray-300 px-4 py-2 text-center">Reason</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {status.result.errors.map((error, idx) => (
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
                            )}

                            {status.result.errors.length === 0 && (
                                <p className="mt-2 text-green-600">
                                    File processed successfully with no errors.
                                </p>
                            )}
                        </div>
                    )}

                    {/* If finished or in-progress but there's an error */}
                    {!status.inProgress && status.errorMessage && (
                        <div className="text-red-500 mt-4">
                            <p>Processing failed with error:</p>
                            <p>{status.errorMessage}</p>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default CsvUploadForm;
