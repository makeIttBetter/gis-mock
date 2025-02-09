"use client";

import React, { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { RealEstate } from "@/interfaces/RealEstate";
import { RealEstateFilterParams } from "@/interfaces/RealEstateFilterParams";
import { RealEstateMapDto } from "@/interfaces/RealEstateMapDto";

// NEW imports for map data & paginated data
import {
    fetchAttachedRealEstate,
    fetchRealEstateMapData,
    fetchRealEstatePaginated,
} from "@/lib/realEstateApi";
import { createPolygon, fetchPolygons, updatePolygon } from "@/lib/polygonApi";
import { PolygonDTO } from "@/interfaces/PolygonDTO";

// UI components
import RealEstateFilterForm from "@/components/RealEstateFilterForm";
import RealEstateList from "@/components/RealEstateList";
import RealEstateMap from "@/components/map/RealEstateMap";
import PolygonsList from "@/components/PolygonsList";

// For pagination controls
import { PaginationControls } from "@/components/PaginationControls";
import { PaginationDTO } from "@/interfaces/PaginationDTO";

/**
 * The RealEstateDashboard displays:
 * 1) A filter form
 * 2) A paginated list of full RealEstate records
 * 3) A map with all matching properties (in minimal form), plus polygon logic
 * 4) A list of polygons
 */
export default function RealEstateDashboard() {
    const router = useRouter();
    const searchParams = useSearchParams();

    // 1) We store the filter fields in local state
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

    // 2) Polygons
    const [polygons, setPolygons] = useState<PolygonDTO[]>([]);
    const [selectedPolygon, setSelectedPolygon] = useState<PolygonDTO | null>(
        null
    );
    const [editMode, setEditMode] = useState<boolean>(false);

    // 3) The map’s minimal real-estate data (potentially large set)
    const [mapRealEstates, setMapRealEstates] = useState<RealEstateMapDto[]>([]);
    const [, setMapLoading] = useState<boolean>(false);

    // 4) The attached real estate (full details) for the selected polygon
    const [attachedRealEstates, setAttachedRealEstates] = useState<RealEstate[]>(
        []
    );

    // 5) The “Properties List” (paginated) with full details
    const [listRealEstates, setListRealEstates] = useState<RealEstate[]>([]);
    const [listLoading, setListLoading] = useState<boolean>(false);

    // 6) Pagination states for the “Properties List”
    const [listPage, setListPage] = useState<number>(1);
    const [listPageSize] = useState<number>(100);
    const [listTotalPages, setListTotalPages] = useState<number>(1);
    const [listTotalElements, setListTotalElements] = useState<number>(0);

    // --- parse URL search parameters into filters:
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
            basementSqFtMin: params.basementSqFtMin
                ? Number(params.basementSqFtMin)
                : undefined,
            basementSqFtMax: params.basementSqFtMax
                ? Number(params.basementSqFtMax)
                : undefined,
            basementFinished: params.basementFinished
                ? params.basementFinished === "true"
                : undefined,
            daysBackMin: params.daysBackMin ? Number(params.daysBackMin) : undefined,
            daysBackMax: params.daysBackMax ? Number(params.daysBackMax) : undefined,
        });
    }, [searchParams]);

    // --- (A) Load ALL polygon data
    async function loadPolygons() {
        try {
            const data = await fetchPolygons();
            setPolygons(data);

            // If the user had a polygon selected, check if it still exists
            if (selectedPolygon) {
                const stillExists = data.find((p) => p.id === selectedPolygon.id);
                if (!stillExists) {
                    // If the polygon was deleted, clear it from the map
                    setSelectedPolygon(null);
                    setEditMode(false);
                }
            }
        } catch (error) {
            console.error("Error fetching polygons:", error);
        }
    }

    useEffect(() => {
        loadPolygons();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // Listen for "polygonDeleted" events from PolygonsList
    useEffect(() => {
        function handlePolygonDeleted() {
            loadPolygons();
        }
        window.addEventListener("polygonDeleted", handlePolygonDeleted);
        return () => {
            window.removeEventListener("polygonDeleted", handlePolygonDeleted);
        };
    }, []);

    // --- (B) Fetch minimal data for the map
    useEffect(() => {
        async function loadMapData() {
            setMapLoading(true);
            try {
                const data = await fetchRealEstateMapData(filters);
                setMapRealEstates(data);
            } catch (error) {
                console.error("Error fetching map real estate data:", error);
            } finally {
                setMapLoading(false);
            }
        }

        loadMapData();
    }, [filters]);

    // --- (C) Fetch a paginated subset for the “Properties List”
    useEffect(() => {
        async function loadListData() {
            setListLoading(true);
            try {
                const paged = await fetchRealEstatePaginated(filters, listPage, listPageSize);
                setListRealEstates(paged.content);
                setListTotalPages(paged.totalPages);
                setListTotalElements(paged.totalElements);
            } catch (error) {
                console.error("Error fetching paginated real estate:", error);
            } finally {
                setListLoading(false);
            }
        }

        loadListData();
    }, [filters, listPage, listPageSize]);

    // --- (D) If a polygon is selected, load its attached real estate
    useEffect(() => {
        async function loadAttached() {
            if (selectedPolygon && selectedPolygon.realEstateObjects.length > 0) {
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
                setAttachedRealEstates([]);
            }
        }

        loadAttached();
    }, [selectedPolygon]);

    // --- (E) When filters change, update the URL & re-fetch
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
            basementFinished:
                merged.basementFinished !== undefined
                    ? String(merged.basementFinished)
                    : "",
            daysBackMin: merged.daysBackMin?.toString() || "",
            daysBackMax: merged.daysBackMax?.toString() || "",
        };
        const query = new URLSearchParams(queryParams).toString();
        router.replace(`?${query}`, { scroll: false });
    };

    // --- (F) Polygon update callback
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

                // Reload the full polygon list
                const updatedPolygons = await fetchPolygons();
                setPolygons(updatedPolygons);

                window.dispatchEvent(new Event("polygonCreated")); // to refresh in PolygonsList
                setEditMode(false);
            } catch (error) {
                console.error("Error updating polygon:", error);
                alert("Failed to update polygon.");
            }
        }
    };

    // --- (G) Polygon create callback
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

    // --- (H) Show/hide attached vs not-attached markers
    const [showAttached, setShowAttached] = useState(true);
    const [showNotAttached, setShowNotAttached] = useState(true);

    // Convert full RealEstate to minimal map shape
    function convertFullToMapDto(item: RealEstate): RealEstateMapDto {
        return {
            id: String(item.id),
            latitude: item.latitude ? parseFloat(item.latitude) : null,
            longitude: item.longitude ? parseFloat(item.longitude) : null,
            city: item.city,
            state: item.state,
            status: item.status,
        };
    }

    const attachedMapData: RealEstateMapDto[] = attachedRealEstates.map(
        convertFullToMapDto
    );
    const attachedIdsSet = new Set(selectedPolygon?.realEstateObjects || []);
    const notAttachedMapData: RealEstateMapDto[] = mapRealEstates.filter(
        (re) => !attachedIdsSet.has(re.id)
    );

    let finalRealEstates: RealEstateMapDto[] = [];
    if (selectedPolygon) {
        if (showAttached) finalRealEstates = finalRealEstates.concat(attachedMapData);
        if (showNotAttached) finalRealEstates = finalRealEstates.concat(notAttachedMapData);
    } else {
        // No polygon selected -> show all markers
        finalRealEstates = mapRealEstates;
    }

    // Pagination object for the “Properties List”
    const paginationObj: PaginationDTO = {
        page: listPage,
        page_size: listPageSize,
        total_pages: listTotalPages,
        total_count: listTotalElements,
    };

    return (
        <div className="p-4 min-h-screen bg-gray-100">
            <h1 className="text-2xl font-bold mb-4">Real Estate Dashboard</h1>

            {/* Filter Form */}
            <RealEstateFilterForm filters={filters} onChange={handleFilterChange} />

            <div className="flex flex-col lg:flex-row mt-4 gap-4">
                {/* Left side: Paginated List */}
                <div className="flex-1">
                    <h2 className="text-xl font-semibold mb-2">
                        Properties List (showing {listRealEstates.length} of {listTotalElements})
                    </h2>
                    {listLoading ? (
                        <div>Loading properties...</div>
                    ) : (
                        <RealEstateList realEstates={listRealEstates} />
                    )}

                    {/* Pagination Controls */}
                    <PaginationControls
                        pagination={paginationObj}
                        onPageChange={(newPage) => setListPage(newPage)}
                    />
                </div>

                {/* Right side: Map + Polygons List */}
                <div className="flex-1 flex flex-col gap-4">
                    {/* Map Section */}
                    <div className="border p-2" style={{ minWidth: "300px" }}>
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

                        {/* Checkboxes to show/hide "attached" vs "not-attached" markers */}
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
                            realEstates={finalRealEstates}
                            attachedIds={
                                selectedPolygon ? selectedPolygon.realEstateObjects : undefined
                            }
                            center={{ lat: 40.114955, lng: -111.654923 }}
                            zoom={11}
                            containerStyle={{ width: "100%", height: "400px" }}
                            displayPolygon={
                                selectedPolygon && !editMode ? selectedPolygon : undefined
                            }
                            editablePolygon={
                                selectedPolygon && editMode ? selectedPolygon : undefined
                            }
                            onUpdatePolygon={handleUpdatePolygon}
                            onCreatePolygon={handleCreatePolygon}
                        />
                    </div>

                    {/* Polygons List Section */}
                    <div className="border p-2">
                        <h2 className="text-xl font-semibold mb-2">Saved Polygons</h2>
                        <PolygonsList />
                    </div>
                </div>
            </div>
        </div>
    );
}
