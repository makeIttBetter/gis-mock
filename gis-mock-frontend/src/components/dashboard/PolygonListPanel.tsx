"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { PolygonDTO } from "@/interfaces/PolygonDTO";
import { RealEstate } from "@/interfaces/RealEstate";
import { fetchPolygons, deletePolygon, exportPolygonCsv } from "@/lib/polygonApi";
import { fetchAttachedRealEstate } from "@/lib/realEstateApi";

/**
 * Renders a list of saved polygons with "Expand," "Edit," "Delete," and "Export CSV" actions.
 */
export default function PolygonListPanel() {
    const [polygons, setPolygons] = useState<PolygonDTO[]>([]);
    const [expandedPolygonId, setExpandedPolygonId] = useState<string | null>(null);
    const [expandedPolygonRealEstates, setExpandedPolygonRealEstates] = useState<{
        [polygonId: string]: RealEstate[];
    }>({});
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

    async function handleToggleExpand(polygon: PolygonDTO) {
        if (expandedPolygonId === polygon.id) {
            setExpandedPolygonId(null);
            return;
        }
        setExpandedPolygonId(polygon.id);
        if (!expandedPolygonRealEstates[polygon.id] && polygon.realEstateObjects.length > 0) {
            try {
                const reList = await fetchAttachedRealEstate(polygon.realEstateObjects);
                setExpandedPolygonRealEstates((prev) => ({
                    ...prev,
                    [polygon.id]: reList,
                }));
            } catch (error) {
                console.error("Error fetching attached real estate:", error);
            }
        }
    }

    async function handleDelete(id: string) {
        if (
            window.confirm("Are you sure you want to delete this polygon and all its relationships?")
        ) {
            try {
                await deletePolygon(id);
                alert("Polygon deleted");
                loadData();
                window.dispatchEvent(new Event("polygonDeleted"));
            } catch (error: any) {
                console.error("Error deleting polygon:", error);
                alert("Failed to delete polygon");
            }
        }
    }

    async function handleExportCsv(polygon: PolygonDTO) {
        if (!window.confirm("Export all attached Real Estate objects as CSV?")) return;
        try {
            setExportLoadingId(polygon.id);
            const blob = await exportPolygonCsv(polygon.id);
            const safeName = polygon.name
                .replace(/[^a-z0-9_\-]+/gi, "_")
                .replace(/_+/g, "_");
            const filename = safeName || "polygon";

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
                const attachedReList = expandedPolygonRealEstates[polygon.id] || [];

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
                                    onClick={() => handleToggleExpand(polygon)}
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

                                <button
                                    onClick={() => handleExportCsv(polygon)}
                                    className="px-3 py-1 bg-purple-600 text-white rounded flex items-center gap-2"
                                    disabled={exportLoadingId === polygon.id}
                                >
                                    {exportLoadingId === polygon.id ? "Exporting..." : "Export to CSV"}
                                </button>
                            </div>
                        </div>

                        {isExpanded && (
                            <div className="mt-3 ml-4 border-l pl-4">
                                <h3 className="font-bold mb-2">Real Estate Objects:</h3>
                                {attachedReList.length === 0 && polygon.realEstateObjects.length > 0 ? (
                                    <div>Loading attached real estate...</div>
                                ) : attachedReList.length === 0 ? (
                                    <div>No attached real estate found.</div>
                                ) : (
                                    attachedReList.map((re) => (
                                        <div key={re.id} className="text-sm">
                                            • MLS#: {re.mlsNumber} — {re.address}, {re.city}, {re.state}
                                        </div>
                                    ))
                                )}
                            </div>
                        )}
                    </div>
                );
            })}
        </div>
    );
}
