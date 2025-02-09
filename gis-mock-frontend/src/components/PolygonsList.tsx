"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { PolygonDTO } from "@/interfaces/PolygonDTO";
// Our polygonsApi with standardized calls
import {
    fetchPolygons,
    deletePolygon,
    exportPolygonCsv,
} from "@/lib/polygonApi";

/**
 * Renders a list of saved polygons, each with an "Expand", "Edit", "Delete", and "Export to CSV" button.
 */
export default function PolygonsList() {
    const [polygons, setPolygons] = useState<PolygonDTO[]>([]);
    const [expandedPolygonId, setExpandedPolygonId] = useState<string | null>(null);

    // Track which polygon is currently exporting (for a spinner)
    const [exportLoadingId, setExportLoadingId] = useState<string | null>(null);

    /**
     * Loads all polygons from the backend.
     */
    async function loadData() {
        try {
            const data = await fetchPolygons();
            setPolygons(data);
        } catch (error) {
            console.error("Error fetching polygons:", error);
        }
    }

    // Initial fetch on mount
    useEffect(() => {
        loadData();
    }, []);

    // Re-fetch whenever these global events fire
    useEffect(() => {
        function handlePolygonCreated() {
            loadData();
        }
        function handlePolygonDeleted() {
            loadData();
        }
        window.addEventListener("polygonCreated", handlePolygonCreated);
        window.addEventListener("polygonDeleted", handlePolygonDeleted);

        return () => {
            window.removeEventListener("polygonCreated", handlePolygonCreated);
            window.removeEventListener("polygonDeleted", handlePolygonDeleted);
        };
    }, []);

    function handleToggleExpand(polygonId: string) {
        setExpandedPolygonId((prev) => (prev === polygonId ? null : polygonId));
    }

    /**
     * Deletes the polygon if user confirms.
     */
    async function handleDelete(id: string) {
        if (
            window.confirm(
                "Are you sure you want to delete this polygon and all its relationships?"
            )
        ) {
            try {
                await deletePolygon(id);
                alert("Polygon deleted");
                loadData();

                // Notify the rest of the app
                window.dispatchEvent(new Event("polygonDeleted"));
            } catch (error: any) {
                console.error("Error deleting polygon:", error);
                alert("Failed to delete polygon");
            }
        }
    }

    /**
     * Export the polygon's attached RealEstate as CSV, naming the file with the polygon's name.
     */
    async function handleExportCsv(polygon: PolygonDTO) {
        if (!window.confirm("Export all attached Real Estate objects as CSV?")) {
            return;
        }
        try {
            setExportLoadingId(polygon.id);

            // 1) Fetch CSV blob
            const blob = await exportPolygonCsv(polygon.id);

            // 2) Create a sanitized name for the downloaded file
            //    (remove or adjust if you want to allow spaces/symbols)
            const safeName = polygon.name
                .replace(/[^a-z0-9_\-]+/gi, "_") // replace non-alphanumeric with underscores
                .replace(/_+/g, "_");          // collapse multiple underscores
            const filename = safeName || "polygon"; // fallback if name is empty

            // 3) Create a link to download
            const downloadUrl = URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = downloadUrl;
            link.setAttribute("download", `${filename}.csv`);
            document.body.appendChild(link);
            link.click();
            link.remove();
            URL.revokeObjectURL(downloadUrl);

        } catch (error: any) {
            console.error("Error exporting CSV:", error);
            alert("Failed to export CSV: " + error.message);
        } finally {
            setExportLoadingId(null);
        }
    }

    return (
        <div className="p-4">
            <h1 className="text-xl font-bold mb-4">Saved Polygons</h1>
            {polygons.map((polygon) => {
                const isExpanded = expandedPolygonId === polygon.id;
                return (
                    <div
                        key={polygon.id}
                        className="bg-white shadow p-4 mb-4 rounded border"
                    >
                        <div className="flex justify-between items-center">
                            <div>
                                <h2 className="font-semibold text-lg">{polygon.name}</h2>
                                <p className="text-sm text-gray-600">
                                    Created: {polygon.dateCreated} | Updated: {polygon.dateUpdated}
                                </p>
                                <p>{`Objects in Polygon: ${polygon.realEstateObjects.length}`}</p>
                            </div>
                            <div className="flex gap-2">
                                <button
                                    onClick={() => handleToggleExpand(polygon.id)}
                                    className="px-3 py-1 bg-blue-500 text-white rounded"
                                >
                                    {isExpanded ? "Collapse" : "Expand"}
                                </button>

                                <Link
                                    href={`/polygons/${polygon.id}/edit`}
                                    className="px-3 py-1 bg-green-500 text-white rounded"
                                >
                                    Edit ↗
                                </Link>

                                <button
                                    onClick={() => handleDelete(polygon.id)}
                                    className="px-3 py-1 bg-red-500 text-white rounded"
                                >
                                    Delete
                                </button>

                                {/* Export to CSV button */}
                                <button
                                    onClick={() => handleExportCsv(polygon)}
                                    className="px-3 py-1 bg-purple-600 text-white rounded flex items-center gap-2"
                                    disabled={exportLoadingId === polygon.id}
                                >
                                    {exportLoadingId === polygon.id ? (
                                        <>
                                            <svg
                                                className="animate-spin h-5 w-5 text-white"
                                                xmlns="http://www.w3.org/2000/svg"
                                                fill="none"
                                                viewBox="0 0 24 24"
                                            >
                                                <circle
                                                    className="opacity-25"
                                                    cx="12"
                                                    cy="12"
                                                    r="10"
                                                    stroke="currentColor"
                                                    strokeWidth="4"
                                                />
                                                <path
                                                    className="opacity-75"
                                                    fill="currentColor"
                                                    d="M4 12a8 8 0 018-8V0C5.373
                            0 0 5.373 0 12h4z"
                                                />
                                            </svg>
                                            Exporting...
                                        </>
                                    ) : (
                                        "Export to CSV"
                                    )}
                                </button>
                            </div>
                        </div>

                        {isExpanded && (
                            <div className="mt-3 ml-4 border-l pl-4">
                                <h3 className="font-bold mb-2">Real Estate Objects:</h3>
                                {polygon.realEstateObjects.map((reId) => (
                                    <div key={reId} className="text-sm">
                                        • RealEstate ID: {reId}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                );
            })}
        </div>
    );
}
