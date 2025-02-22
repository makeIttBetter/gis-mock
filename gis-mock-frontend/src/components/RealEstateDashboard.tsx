"use client";

import React, {useEffect, useState} from "react";
import {useRouter, useSearchParams} from "next/navigation";
import {RealEstateFilterParams} from "@/interfaces/RealEstateFilterParams";
import {RealEstateMapDto} from "@/interfaces/RealEstateMapDto";

import {fetchRealEstateMapData, fetchRealEstatePaginated,} from "@/lib/realEstateApi";
import {createPolygon, fetchPolygons, updatePolygon} from "@/lib/polygonApi";
import {PolygonDTO} from "@/interfaces/PolygonDTO";

import RealEstateFilterForm from "@/components/RealEstateFilterForm";
import RealEstateList from "@/components/RealEstateList";
import RealEstateMap from "@/components/map/RealEstateMap";
import PolygonsList from "@/components/PolygonsList";
import {PaginationControls} from "@/components/PaginationControls";
import {RealEstate} from "@/interfaces/RealEstate";
import {PaginationDTO} from "@/interfaces/PaginationDTO";

export default function RealEstateDashboard() {
    const router = useRouter();
    const searchParams = useSearchParams();

    // (1) State for filters
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

    // (2) Polygons
    const [polygons, setPolygons] = useState<PolygonDTO[]>([]);
    const [selectedPolygon, setSelectedPolygon] = useState<PolygonDTO | null>(null);
    const [editMode, setEditMode] = useState(false);

    // (3) Map real-estate data (unified array with all fields)
    const [mapRealEstates, setMapRealEstates] = useState<RealEstateMapDto[]>([]);
    const [, setMapLoading] = useState(false);

    // (4) For the “Properties List” (paginated)
    const [listRealEstates, setListRealEstates] = useState<RealEstate[]>([]);
    const [listLoading, setListLoading] = useState<boolean>(false);
    const [listPage, setListPage] = useState<number>(1);
    const [listPageSize] = useState<number>(100);
    const [listTotalPages, setListTotalPages] = useState<number>(1);
    const [listTotalElements, setListTotalElements] = useState<number>(0);

    // (5) Show/hide attached vs not-attached
    const [showAttached, setShowAttached] = useState<boolean>(true);
    const [showNotAttached, setShowNotAttached] = useState<boolean>(true);

    // ------------------------------------------------------------
    // Read URL search parameters => set initial filters
    // ------------------------------------------------------------
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
            basementFinished:
                params.basementFinished !== undefined
                    ? params.basementFinished === "true"
                    : undefined,
            daysBackMin: params.daysBackMin ? Number(params.daysBackMin) : undefined,
            daysBackMax: params.daysBackMax ? Number(params.daysBackMax) : undefined,
        });
    }, [searchParams]);

    // ------------------------------------------------------------
    // Load polygons (used for polygon dropdown + listing)
    // ------------------------------------------------------------
    async function loadPolygons() {
        try {
            const list = await fetchPolygons();
            setPolygons(list);

            // If user had a polygon selected, ensure it still exists
            if (selectedPolygon) {
                const stillExists = list.find((p) => p.id === selectedPolygon.id);
                if (!stillExists) {
                    setSelectedPolygon(null);
                    setEditMode(false);
                }
            }
        } catch (err) {
            console.error("Error loading polygons:", err);
        }
    }

    useEffect(() => {
        loadPolygons();
    }, []);

    // Listen for polygonDeleted events to reload polygons
    useEffect(() => {
        function handlePolygonDeleted() {
            loadPolygons();
        }

        window.addEventListener("polygonDeleted", handlePolygonDeleted);
        return () => {
            window.removeEventListener("polygonDeleted", handlePolygonDeleted);
        };
    }, []);

    // ------------------------------------------------------------
    // Load map data (RealEstateMapDto) for all real-estate matching filters
    // ------------------------------------------------------------
    useEffect(() => {
        async function loadMapData() {
            setMapLoading(true);
            try {
                // fetchRealEstateMapData returns every field we need
                const data = await fetchRealEstateMapData(filters);
                setMapRealEstates(data); // store them in one unified array
            } catch (error) {
                console.error("Error loading map data:", error);
            } finally {
                setMapLoading(false);
            }
        }

        loadMapData();
    }, [filters]);

    // ------------------------------------------------------------
    // Load the “Properties List” (Paginated RealEstate) for the table
    // ------------------------------------------------------------
    useEffect(() => {
        async function loadList() {
            setListLoading(true);
            try {
                const paged = await fetchRealEstatePaginated(filters, listPage, listPageSize);
                setListRealEstates(paged.content);
                setListTotalPages(paged.totalPages);
                setListTotalElements(paged.totalElements);
            } catch (err) {
                console.error("Error loading paginated real estate:", err);
            } finally {
                setListLoading(false);
            }
        }

        loadList();
    }, [filters, listPage, listPageSize]);

    // ------------------------------------------------------------
    // Handle filter changes => update the URL
    // ------------------------------------------------------------
    function handleFilterChange(newFilters: RealEstateFilterParams) {
        const merged = {...filters, ...newFilters};
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
        router.replace(`?${query}`, {scroll: false});
    }

    // ------------------------------------------------------------
    // Polygon create + update callbacks
    // ------------------------------------------------------------
    async function handleCreatePolygon(newPolygon: {
        name: string;
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) {
        try {
            const created = await createPolygon(
                newPolygon.name,
                newPolygon.coordinates,
                newPolygon.realEstateIds
            );
            alert("New polygon created successfully!");
            await loadPolygons(); // refresh polygons list
            setSelectedPolygon(created);
            window.dispatchEvent(new Event("polygonCreated"));
        } catch (err) {
            console.error("Error creating polygon:", err);
            alert("Failed to create polygon.");
        }
    }

    async function handleUpdatePolygon(updated: {
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) {
        if (!selectedPolygon) return;
        try {
            const result = await updatePolygon(selectedPolygon.id, {
                name: selectedPolygon.name,
                coordinates: updated.coordinates,
                realEstateIds: updated.realEstateIds,
            });
            alert("Polygon updated successfully!");
            setSelectedPolygon(result);
            await loadPolygons();
            window.dispatchEvent(new Event("polygonCreated"));
            setEditMode(false);
        } catch (error) {
            console.error("Error updating polygon:", error);
            alert("Failed to update polygon.");
        }
    }

    // ------------------------------------------------------------
    // Build final array for the map (attached + not attached)
    // ------------------------------------------------------------
    const attachedIdsSet = new Set(selectedPolygon?.realEstateObjects || []);

    // We'll filter from the single array `mapRealEstates`:
    let finalMapData: RealEstateMapDto[] = [];

    if (selectedPolygon) {
        // If a polygon is selected, we want to show either attached or not attached or both
        const attachedArray = mapRealEstates.filter((re) =>
            attachedIdsSet.has(re.id)
        );
        const notAttachedArray = mapRealEstates.filter(
            (re) => !attachedIdsSet.has(re.id)
        );

        if (showAttached) finalMapData = finalMapData.concat(attachedArray);
        if (showNotAttached) finalMapData = finalMapData.concat(notAttachedArray);
    } else {
        // If no polygon is selected, we just show everything
        finalMapData = mapRealEstates;
    }

    // ------------------------------------------------------------
    // Prepare pagination object
    // ------------------------------------------------------------
    const paginationObj: PaginationDTO = {
        page: listPage,
        page_size: listPageSize,
        total_pages: listTotalPages,
        total_count: listTotalElements,
    };

    return (
        <div className="p-4 min-h-screen bg-gray-100">
            <h1 className="text-2xl font-bold mb-4">Real Estate Dashboard</h1>

            {/* Filter form */}
            <RealEstateFilterForm filters={filters} onChange={handleFilterChange}/>

            <div className="flex flex-col lg:flex-row mt-4 gap-4">
                {/* LEFT: Paginated RealEstate list */}
                <div className="flex-1">
                    <h2 className="text-xl font-semibold mb-2">
                        Properties List (showing {listRealEstates.length} of {listTotalElements})
                    </h2>
                    {listLoading ? (
                        <div>Loading properties...</div>
                    ) : (
                        <RealEstateList realEstates={listRealEstates}/>
                    )}

                    <PaginationControls
                        pagination={paginationObj}
                        onPageChange={(newPage) => setListPage(newPage)}
                    />
                </div>

                {/* RIGHT: Map + Polygons */}
                <div className="flex-1 flex flex-col gap-4">
                    <div className="border p-2" style={{minWidth: "300px"}}>
                        <h2 className="text-xl font-semibold mb-2">Map View</h2>

                        {/* Polygon dropdown */}
                        <div className="mb-2 flex items-center gap-2">
                            <label className="text-sm font-medium">Select Polygon:</label>
                            <select
                                value={selectedPolygon ? selectedPolygon.id : ""}
                                onChange={(e) => {
                                    const poly = polygons.find((p) => p.id === e.target.value) || null;
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

                        {/* Show/hide checkboxes (attached vs not-attached) */}
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
                            realEstates={finalMapData} // full array (both attached + not attached)
                            attachedIds={selectedPolygon?.realEstateObjects}
                            center={{lat: 40.114955, lng: -111.654923}}
                            zoom={11}
                            containerStyle={{width: "100%", height: "400px"}}
                            displayPolygon={
                                selectedPolygon && !editMode ? selectedPolygon : undefined
                            }
                            editablePolygon={selectedPolygon && editMode ? selectedPolygon : undefined}
                            onUpdatePolygon={handleUpdatePolygon}
                            onCreatePolygon={handleCreatePolygon}
                        />
                    </div>

                    {/* Polygons list */}
                    <div className="border p-2">
                        <h2 className="text-xl font-semibold mb-2">Saved Polygons</h2>
                        <PolygonsList/>
                    </div>
                </div>
            </div>
        </div>
    );
}
