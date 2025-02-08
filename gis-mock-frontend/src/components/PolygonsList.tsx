// File: C:/ws/projects/GIS-Michael/gis-mock/gis-mock-frontend/src/components/PolygonsList.tsx

"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { deletePolygon, exportPolygonToGoogleSheets, fetchPolygons } from "@/lib/polygonApi";
import { PolygonDTO } from "@/interfaces/PolygonDTO";

export default function PolygonsList() {
    const [polygons, setPolygons] = useState<PolygonDTO[]>([]);
    const [expandedPolygonId, setExpandedPolygonId] = useState<string | null>(null);

    // Loading state for the "Export to Google Sheets" button
    const [exportLoadingId, setExportLoadingId] = useState<string | null>(null);

    async function loadData() {
        try {
            const data = await fetchPolygons();
            setPolygons(data);
        } catch (error) {
            console.error("Error fetching polygons:", error);
        }
    }

    useEffect(() => {
        loadData();
    }, []);

    useEffect(() => {
        const handlePolygonCreated = () => {
            loadData();
        };
        window.addEventListener("polygonCreated", handlePolygonCreated);
        return () => {
            window.removeEventListener("polygonCreated", handlePolygonCreated);
        };
    }, []);

    function handleToggleExpand(polygonId: string) {
        setExpandedPolygonId((prev) => (prev === polygonId ? null : polygonId));
    }

    async function handleDelete(id: string) {
        if (
            window.confirm("Are you sure you want to delete this polygon and all its relationships?")
        ) {
            try {
                await deletePolygon(id);
                alert("Polygon deleted");
                loadData();
            } catch (error) {
                console.error("Error deleting polygon:", error);
                alert("Failed to delete polygon");
            }
        }
    }

    // NEW: Handle the "Export to Google Sheets" action
    async function handleExport(id: string) {
        if (!window.confirm("Export all attached Real Estate objects to Google Sheets?")) {
            return;
        }
        try {
            setExportLoadingId(id);
            const sheetUrl = await exportPolygonToGoogleSheets(id);
            // Once successful, redirect user to the Google Sheet
            window.location.href = sheetUrl;
        } catch (error: any) {
            console.error("Error exporting polygon data:", error);
            alert("Failed to export polygon data: " + error.message);
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
                    <div key={polygon.id} className="bg-white shadow p-4 mb-4 rounded border">
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
                                {/* "Export to Google Sheets" button (far right) */}
                                <button
                                    onClick={() => handleExport(polygon.id)}
                                    className="px-3 py-1 bg-purple-600 text-white rounded flex items-center gap-2"
                                    disabled={exportLoadingId === polygon.id}
                                >
                                    {exportLoadingId === polygon.id ? (
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
                                            ></circle>
                                            <path
                                                className="opacity-75"
                                                fill="currentColor"
                                                d="M4 12a8 8 0 018-8V0C5.373
                              0 0 5.373 0 12h4z"
                                            ></path>
                                        </svg>
                                    ) : null}
                                    {exportLoadingId === polygon.id
                                        ? "Exporting..."
                                        : "Export to Google Sheets"}
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
