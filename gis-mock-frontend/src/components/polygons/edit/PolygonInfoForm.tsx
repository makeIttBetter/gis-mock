"use client";

import React from "react";

interface PolygonInfoFormProps {
    polygonName: string;
    setPolygonName: (name: string) => void;
}

/**
 * Simple form for polygon name (and could add ArcGIS layer fields).
 */
export default function PolygonInfoForm({
                                            polygonName,
                                            setPolygonName,
                                        }: PolygonInfoFormProps) {
    return (
        <div className="bg-white p-4 rounded shadow">
            <h2 className="text-xl font-semibold mb-2">Edit Polygon Info</h2>
            <label className="block mb-2">
                <span className="text-sm font-medium">Polygon Name:</span>
                <input
                    type="text"
                    className="mt-1 block w-full border border-gray-300 rounded p-1"
                    value={polygonName}
                    onChange={(e) => setPolygonName(e.target.value)}
                />
            </label>
        </div>
    );
}
