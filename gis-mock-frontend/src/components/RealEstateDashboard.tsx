// File: src/components/RealEstateDashboard.tsx
"use client";

import React, { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { RealEstate } from "@/interfaces/RealEstate";
import { fetchAttachedRealEstate, fetchRealEstateData } from "@/lib/realEstateApi";
import RealEstateFilterForm from "@/components/RealEstateFilterForm";
import { RealEstateFilterParams } from "@/interfaces/RealEstateFilterParams";
import RealEstateList from "@/components/RealEstateList";
import RealEstateMap from "@/components/map/RealEstateMap";
import PolygonsList from "@/components/PolygonsList";
import { createPolygon, fetchPolygons, updatePolygon } from "@/lib/polygonApi";
import { PolygonDTO } from "@/interfaces/PolygonDTO";

export default function RealEstateDashboard() {
    const router = useRouter();
    const searchParams = useSearchParams();

    // Initialize filter state with both existing and new filter fields.
    const [filters, setFilters] = useState<RealEstateFilterParams>({
        city: "",
        state: "",
        status: "",
        minPrice: "",
        maxPrice: "",
        ids: "",
        address: "",
        zipcode: "",
        propertyTypes: [],
        styles: [],
        yearBuiltMin: undefined,
        yearBuiltMax: undefined,
        glaMin: undefined,
        glaMax: undefined,
        basementSqFtMin: undefined,
        basementSqFtMax: undefined,
        basementFinished: undefined,
        daysBackMin: undefined,
        daysBackMax: undefined,
    });
    const [realEstates, setRealEstates] = useState<RealEstate[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [polygons, setPolygons] = useState<PolygonDTO[]>([]);
    const [selectedPolygon, setSelectedPolygon] = useState<PolygonDTO | null>(null);
    const [editMode, setEditMode] = useState<boolean>(false);
    const [showAttached, setShowAttached] = useState(true);
    const [showNotAttached, setShowNotAttached] = useState(true);
    const [attachedRealEstates, setAttachedRealEstates] = useState<RealEstate[]>([]);

    // Parse URL search parameters into filters.
    useEffect(() => {
        if (!searchParams) return;
        const params = Object.fromEntries(searchParams.entries());
        setFilters({
            city: params.city || "",
            state: params.state || "",
            status: params.status || "",
            minPrice: params.minPrice || "",
            maxPrice: params.maxPrice || "",
            ids: params.ids || "",
            address: params.address || "",
            zipcode: params.zipcode || "",
            propertyTypes: params.propertyTypes ? params.propertyTypes.split(",") : [],
            styles: params.styles ? params.styles.split(",") : [],
            yearBuiltMin: params.yearBuiltMin ? Number(params.yearBuiltMin) : undefined,
            yearBuiltMax: params.yearBuiltMax ? Number(params.yearBuiltMax) : undefined,
            glaMin: params.glaMin ? Number(params.glaMin) : undefined,
            glaMax: params.glaMax ? Number(params.glaMax) : undefined,
            basementSqFtMin: params.basementSqFtMin ? Number(params.basementSqFtMin) : undefined,
            basementSqFtMax: params.basementSqFtMax ? Number(params.basementSqFtMax) : undefined,
            basementFinished: params.basementFinished ? params.basementFinished === "true" : undefined,
            daysBackMin: params.daysBackMin ? Number(params.daysBackMin) : undefined,
            daysBackMax: params.daysBackMax ? Number(params.daysBackMax) : undefined,
        });
    }, [searchParams]);

    // Fetch real estate data when filters change.
    useEffect(() => {
        async function loadData() {
            setLoading(true);
            try {
                const data = await fetchRealEstateData(filters);
                setRealEstates(data);
            } catch (error) {
                console.error("Error fetching real estate data:", error);
            } finally {
                setLoading(false);
            }
        }
        loadData();
    }, [filters]);

    // Fetch polygons from the backend.
    useEffect(() => {
        async function loadPolygons() {
            try {
                const data = await fetchPolygons();
                setPolygons(data);
            } catch (error) {
                console.error("Error fetching polygons:", error);
            }
        }
        loadPolygons();
    }, []);

    // When a polygon is selected and "show attached" is true, fetch its attached real estate objects.
    useEffect(() => {
        async function loadAttached() {
            if (
                selectedPolygon &&
                showAttached &&
                selectedPolygon.realEstateObjects.length > 0
            ) {
                try {
                    const data = await fetchAttachedRealEstate(
                        selectedPolygon.realEstateObjects
                    );
                    setAttachedRealEstates(data);
                } catch (error) {
                    console.error("Error fetching attached real estate:", error);
                    setAttachedRealEstates([]);
                }
            } else {
                // Clear the attached real estate when there's no polygon or showAttached is off
                setAttachedRealEstates([]);
            }
        }
        loadAttached();
    }, [selectedPolygon, showAttached]);

    // Handle filter changes by merging with current filters and updating URL query parameters.
    const handleFilterChange = (newFilters: RealEstateFilterParams) => {
        const merged = { ...filters, ...newFilters };
        const queryParams: Record<string, string> = {
            city: merged.city || "",
            state: merged.state || "",
            status: merged.status || "",
            minPrice: merged.minPrice || "",
            maxPrice: merged.maxPrice || "",
            ids: merged.ids || "",
            address: merged.address || "",
            zipcode: merged.zipcode || "",
            propertyTypes: merged.propertyTypes ? merged.propertyTypes.join(",") : "",
            styles: merged.styles ? merged.styles.join(",") : "",
            yearBuiltMin: merged.yearBuiltMin?.toString() || "",
            yearBuiltMax: merged.yearBuiltMax?.toString() || "",
            glaMin: merged.glaMin?.toString() || "",
            glaMax: merged.glaMax?.toString() || "",
            basementSqFtMin: merged.basementSqFtMin?.toString() || "",
            basementSqFtMax: merged.basementSqFtMax?.toString() || "",
            basementFinished: merged.basementFinished !== undefined ? merged.basementFinished.toString() : "",
            daysBackMin: merged.daysBackMin?.toString() || "",
            daysBackMax: merged.daysBackMax?.toString() || "",
        };
        const query = new URLSearchParams(queryParams).toString();
        router.replace(`?${query}`, { scroll: false });
    };

    // Callback for updating an existing polygon.
    const handleUpdatePolygon = async (updated: {
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) => {
        if (selectedPolygon) {
            try {
                const updatedPolygon = await updatePolygon(selectedPolygon.id, {
                    name: selectedPolygon.name,
                    coordinates: updated.coordinates,
                    realEstateIds: updated.realEstateIds,
                });
                alert("Polygon updated successfully!");
                setSelectedPolygon(updatedPolygon);
                const updatedPolygons = await fetchPolygons();
                setPolygons(updatedPolygons);
                window.dispatchEvent(new Event("polygonCreated"));
                setEditMode(false);
            } catch (error) {
                console.error("Error updating polygon:", error);
                alert("Failed to update polygon.");
            }
        }
    };

    // Callback for creating a new polygon.
    const handleCreatePolygon = async (newPolygon: {
        name: string;
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) => {
        try {
            const created = await createPolygon(
                newPolygon.name,
                newPolygon.coordinates,
                newPolygon.realEstateIds
            );
            alert("New polygon created successfully!");
            const updatedPolygons = await fetchPolygons();
            setPolygons(updatedPolygons);
            setSelectedPolygon(created);
            window.dispatchEvent(new Event("polygonCreated"));
        } catch (error) {
            console.error("Error creating polygon:", error);
            alert("Failed to create polygon.");
        }
    };

    // Compute the list of real estate objects not attached to the selected polygon.
    const notAttachedRealEstates = selectedPolygon
        ? realEstates.filter(
            (re) => !selectedPolygon.realEstateObjects.includes(re.id.toString())
        )
        : realEstates;

    // Combine markers based on the checkbox selections.
    let finalRealEstates: RealEstate[] = [];
    if (selectedPolygon) {
        if (showAttached) finalRealEstates = finalRealEstates.concat(attachedRealEstates);
        if (showNotAttached)
            finalRealEstates = finalRealEstates.concat(notAttachedRealEstates);
    } else {
        finalRealEstates = realEstates;
    }

    // Extract attached IDs (used for marker coloring).
    const attachedIds =
        selectedPolygon && showAttached
            ? attachedRealEstates.map((re) => re.id.toString())
            : [];

    return (
        <div className="p-4 min-h-screen bg-gray-100">
            <h1 className="text-2xl font-bold mb-4">Real Estate Dashboard</h1>
            {/* Filter Form */}
            <RealEstateFilterForm filters={filters} onChange={handleFilterChange} />
            <div className="flex flex-col lg:flex-row mt-4 gap-4">
                {/* Properties List */}
                <div className="flex-1">
                    <h2 className="text-xl font-semibold mb-2">
                        Properties List ({realEstates.length} found)
                    </h2>
                    {loading ? (
                        <div>Loading properties...</div>
                    ) : (
                        <RealEstateList realEstates={realEstates} />
                    )}
                </div>
                {/* Map and Polygons List */}
                <div className="flex-1 flex flex-col gap-4">
                    <div
                        className="border p-2"
                        style={{ resize: "horizontal", overflow: "auto", minWidth: "300px" }}
                    >
                        <h2 className="text-xl font-semibold mb-2">Map View</h2>
                        {/* Polygon Selection Controls */}
                        <div className="mb-2 flex items-center gap-2">
                            <label className="text-sm font-medium">Select Polygon:</label>
                            <select
                                value={selectedPolygon ? selectedPolygon.id : ""}
                                onChange={(e) => {
                                    const poly =
                                        polygons.find((p) => p.id === e.target.value) || null;
                                    setSelectedPolygon(poly);
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
                                        onClick={() => setSelectedPolygon(null)}
                                        className="px-3 py-1 bg-red-500 text-white rounded"
                                    >
                                        Clear Map
                                    </button>
                                </>
                            )}
                        </div>
                        {/* Checkbox controls – show attached and not attached markers */}
                        {selectedPolygon && (
                            <div className="mb-2 flex gap-4">
                                <label className="flex items-center">
                                    <input
                                        type="checkbox"
                                        checked={showNotAttached}
                                        onChange={(e) => setShowNotAttached(e.target.checked)}
                                        className="mr-1"
                                    />
                                    Show all not attached to polygon
                                </label>
                                <label className="flex items-center">
                                    <input
                                        type="checkbox"
                                        checked={showAttached}
                                        onChange={(e) => setShowAttached(e.target.checked)}
                                        className="mr-1"
                                    />
                                    Show all attached to polygon
                                </label>
                            </div>
                        )}
                        <RealEstateMap
                            key={`${selectedPolygon ? selectedPolygon.id : "none"}-${showAttached}-${showNotAttached}`}
                            realEstates={finalRealEstates}
                            attachedIds={attachedIds}
                            center={{ lat: 40.114955, lng: -111.654923 }}
                            zoom={11}
                            containerStyle={{ width: "100%", height: "400px" }}
                            displayPolygon={selectedPolygon && !editMode ? selectedPolygon : undefined}
                            editablePolygon={selectedPolygon && editMode ? selectedPolygon : undefined}
                            onUpdatePolygon={handleUpdatePolygon}
                            onCreatePolygon={handleCreatePolygon}
                        />
                    </div>
                    <div className="border p-2">
                        <h2 className="text-xl font-semibold mb-2">Saved Polygons</h2>
                        <PolygonsList />
                    </div>
                </div>
            </div>
        </div>
    );
}
