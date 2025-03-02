"use client";

import React from "react";
import { PolygonDTO } from "@/interfaces/PolygonDTO";
import { RealEstateMapDto } from "@/interfaces/RealEstateMapDto";
import RealEstateMap from "@/components/map/RealEstateMap";

interface MapPanelProps {
    polygons: PolygonDTO[];
    selectedPolygon: PolygonDTO | null;
    editMode: boolean;
    setSelectedPolygon: (polygon: PolygonDTO | null) => void;
    setEditMode: (mode: boolean) => void;

    finalMapData: RealEstateMapDto[];
    showNotAttached: boolean;
    setShowNotAttached: (val: boolean) => void;
    showAttached: boolean;
    setShowAttached: (val: boolean) => void;

    onCreatePolygon: (payload: {
        name: string;
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) => void;
    onUpdatePolygon: (payload: {
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) => void;
}

/**
 * Displays a dropdown to select polygons, toggles edit mode,
 * and renders the RealEstateMap component for finalMapData.
 */
export default function MapPanel({
                                     polygons,
                                     selectedPolygon,
                                     editMode,
                                     setSelectedPolygon,
                                     setEditMode,
                                     finalMapData,
                                     showNotAttached,
                                     setShowNotAttached,
                                     showAttached,
                                     setShowAttached,
                                     onCreatePolygon,
                                     onUpdatePolygon,
                                 }: MapPanelProps) {
    return (
        <div className="border p-2" style={{ minWidth: "300px" }}>
            <h2 className="text-xl font-semibold mb-2">Map View</h2>

            {/* Polygon selection + Edit toggles */}
            <div className="mb-2 flex items-center gap-2">
                <label className="text-sm font-medium">Select Polygon:</label>
                <select
                    value={selectedPolygon ? selectedPolygon.id : ""}
                    onChange={(e) => {
                        const poly = polygons.find((p) => p.id === e.target.value);
                        setSelectedPolygon(poly || null);
                        setEditMode(false);
                    }}
                    className="border rounded p-1"
                >
                    <option value="">None</option>
                    {polygons.map((p) => (
                        <option key={p.id} value={p.id}>
                            {p.name}
                        </option>
                    ))}
                </select>

                {selectedPolygon && (
                    <>
                        {editMode ? (
                            <button
                                onClick={() => setEditMode(false)}
                                className="px-3 py-1 bg-yellow-700 text-white rounded"
                            >
                                Cancel Editing
                            </button>
                        ) : (
                            <button
                                onClick={() => setEditMode(true)}
                                className="px-3 py-1 bg-green-500 text-white rounded"
                            >
                                Edit Polygon
                            </button>
                        )}
                        <button
                            onClick={() => {
                                setSelectedPolygon(null);
                                setEditMode(false);
                            }}
                            className="px-3 py-1 bg-red-500 text-white rounded"
                        >
                            Clear Map
                        </button>
                    </>
                )}
            </div>

            {selectedPolygon && (
                <div className="mb-2 flex gap-4">
                    <label className="flex items-center">
                        <input
                            type="checkbox"
                            checked={showNotAttached}
                            onChange={(e) => setShowNotAttached(e.target.checked)}
                            className="mr-1"
                        />
                        Show not-attached
                    </label>
                    <label className="flex items-center">
                        <input
                            type="checkbox"
                            checked={showAttached}
                            onChange={(e) => setShowAttached(e.target.checked)}
                            className="mr-1"
                        />
                        Show attached
                    </label>
                </div>
            )}

            <RealEstateMap
                key={
                    (selectedPolygon ? selectedPolygon.id : "none") +
                    "-" +
                    String(showAttached) +
                    "-" +
                    String(showNotAttached)
                }
                realEstates={finalMapData}
                attachedIds={selectedPolygon?.realEstateObjects}
                // center={{ lat: 40.114955, lng: -111.654923 }}
                zoom={11}
                containerStyle={{ width: "100%", height: "400px" }}
                displayPolygon={selectedPolygon && !editMode ? selectedPolygon : undefined}
                editablePolygon={selectedPolygon && editMode ? selectedPolygon : undefined}
                onUpdatePolygon={onUpdatePolygon}
                onCreatePolygon={onCreatePolygon}
            />
        </div>
    );
}
