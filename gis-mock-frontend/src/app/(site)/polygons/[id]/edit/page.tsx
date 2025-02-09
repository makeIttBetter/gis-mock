// src/app/(site)/polygons/[id]/edit/page.tsx
"use client";

import React, { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import EditablePolygonMap, { LatLng } from "@/components/map/EditablePolygonMap";
import { fetchPolygonById, updatePolygon } from "@/lib/polygonApi";
import { PolygonDTO } from "@/interfaces/PolygonDTO";

export default function PolygonEditPage() {
    const router = useRouter();
    const params = useParams();
    const { id } = params as { id: string };

    const [polygon, setPolygon] = useState<PolygonDTO | null>(null);
    const [name, setName] = useState("");
    const [coordinates, setCoordinates] = useState<LatLng[]>([]);
    const [realEstateObjects, setRealEstateObjects] = useState<string[]>([]);
    const [newREId, setNewREId] = useState("");
    const [loading, setLoading] = useState<boolean>(false);

    useEffect(() => {
        async function loadPolygon() {
            try {
                const data = await fetchPolygonById(id);
                setPolygon(data);
                setName(data.name);
                setCoordinates(data.coordinates);
                setRealEstateObjects(data.realEstateObjects);
            } catch (error) {
                console.error("Error fetching polygon:", error);
            }
        }
        loadPolygon();
    }, [id]);

    const handleAddRE = () => {
        if (newREId.trim() !== "" && !realEstateObjects.includes(newREId.trim())) {
            setRealEstateObjects([...realEstateObjects, newREId.trim()]);
            setNewREId("");
        }
    };

    const handleRemoveRE = (reId: string) => {
        setRealEstateObjects(realEstateObjects.filter((id) => id !== reId));
    };

    const handleSave = async () => {
        setLoading(true);
        try {
            await updatePolygon(id, {
                name,
                coordinates,
                realEstateIds: realEstateObjects,
            });
            alert("Polygon updated successfully!");
            router.push("/polygons");
        } catch (error) {
            console.error("Error updating polygon:", error);
            alert("Failed to update polygon.");
        } finally {
            setLoading(false);
        }
    };

    if (!polygon) {
        return <div>Loading polygon...</div>;
    }

    return (
        <div className="p-4">
            <h1 className="text-2xl font-bold mb-4">Edit Polygon</h1>
            <div className="mb-4">
                <label className="block text-sm font-medium mb-1">
                    Polygon Name:
                </label>
                <input
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="border rounded p-2 w-full"
                />
            </div>
            <div className="mb-4">
                <EditablePolygonMap
                    initialCoordinates={coordinates}
                    onCoordinatesChange={setCoordinates}
                    containerStyle={{ width: "100%", height: "400px" }}
                />
            </div>
            <div className="mb-4">
                <h2 className="text-xl font-semibold mb-2">
                    Real Estate Objects in Polygon
                </h2>
                {realEstateObjects.length === 0 ? (
                    <div>No objects added.</div>
                ) : (
                    <ul className="list-disc pl-5">
                        {realEstateObjects.map((reId) => (
                            <li key={reId} className="flex items-center justify-between">
                                <span>{reId}</span>
                                <button
                                    onClick={() => handleRemoveRE(reId)}
                                    className="px-2 py-1 bg-red-500 text-white rounded text-sm"
                                >
                                    Remove
                                </button>
                            </li>
                        ))}
                    </ul>
                )}
                <div className="mt-2 flex gap-2">
                    <input
                        type="text"
                        placeholder="Enter RE ID"
                        value={newREId}
                        onChange={(e) => setNewREId(e.target.value)}
                        className="border rounded p-1 flex-1"
                    />
                    <button
                        onClick={handleAddRE}
                        className="px-3 py-1 bg-blue-500 text-white rounded"
                    >
                        Add
                    </button>
                </div>
            </div>
            <button
                onClick={handleSave}
                className="px-4 py-2 bg-green-600 text-white rounded"
                disabled={loading}
            >
                {loading ? "Saving..." : "Save Changes"}
            </button>
        </div>
    );
}
