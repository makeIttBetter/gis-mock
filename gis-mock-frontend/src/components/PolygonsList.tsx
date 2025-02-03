// File: frontend/src/components/PolygonsList.tsx
"use client";

import React, { useEffect, useState } from "react";
import { fetchPolygons, deletePolygon } from "@/lib/polygonApi";
import { PolygonDTO } from "@/interfaces/PolygonDTO";

export default function PolygonsList() {
    const [polygons, setPolygons] = useState<PolygonDTO[]>([]);
    const [expandedPolygonId, setExpandedPolygonId] = useState<number | null>(null);

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

    // Listen for the "polygonCreated" event to refresh the list automatically.
    useEffect(() => {
        const handlePolygonCreated = () => {
            loadData();
        };
        window.addEventListener("polygonCreated", handlePolygonCreated);
        return () => {
            window.removeEventListener("polygonCreated", handlePolygonCreated);
        };
    }, []);

    function handleToggleExpand(polygonId: number) {
        setExpandedPolygonId((prev) => (prev === polygonId ? null : polygonId));
    }

    async function handleDelete(id: number) {
        if (
            window.confirm(
                "Are you sure you want to delete this polygon and all its relationships?"
            )
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
                                <button
                                    onClick={() => handleDelete(polygon.id)}
                                    className="px-3 py-1 bg-red-500 text-white rounded"
                                >
                                    Delete
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
