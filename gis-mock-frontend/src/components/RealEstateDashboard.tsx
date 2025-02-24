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

/**
 * The main dashboard that:
 * 1) Shows a filter form,
 * 2) Lists properties (with pagination),
 * 3) Displays a map with optional polygon editing,
 * 4) And a polygons list.
 */
export default function RealEstateDashboard() {
    const router = useRouter();
    const searchParams = useSearchParams();

    // 1) Filter state from the URL
    const [filters, setFilters] = useState<RealEstateFilterParams>({});
    const [didLoadFilters, setDidLoadFilters] = useState(false);

    // 2) Polygons
    const [polygons, setPolygons] = useState<PolygonDTO[]>([]);
    const [selectedPolygon, setSelectedPolygon] = useState<PolygonDTO | null>(null);
    const [editMode, setEditMode] = useState(false);

    // 3) Real estate map data
    //    The backend returns { filtered: [...], attached: [...] }
    const [mapRealEstates, setMapRealEstates] = useState<{
        filtered: RealEstateMapDto[];
        attached: RealEstateMapDto[];
    }>({filtered: [], attached: []});
    const [, setMapLoading] = useState(false);

    // 4) Paginated list data
    const [listRealEstates, setListRealEstates] = useState<RealEstate[]>([]);
    const [listLoading, setListLoading] = useState(false);
    const [listPage, setListPage] = useState<number>(1);
    const [listPageSize] = useState<number>(100);
    const [listTotalPages, setListTotalPages] = useState<number>(1);
    const [listTotalElements, setListTotalElements] = useState<number>(0);

    // 5) Toggles for showing attached / not-attached
    const [showAttached, setShowAttached] = useState(true);
    const [showNotAttached, setShowNotAttached] = useState(true);

    /* ----------------------------------------------------------
     *  1) Parse URL search parameters => set "filters"
     * ---------------------------------------------------------- */
    useEffect(() => {
        if (!searchParams) return;

        const paramsObj: Record<string, string> = Object.fromEntries(
            searchParams.entries()
        );

        function parseBoolOrUndef(val?: string) {
            if (val === "true") return true;
            if (val === "false") return false;
            return undefined;
        }

        const newFilters: RealEstateFilterParams = {
            city: paramsObj.city || "",
            state: paramsObj.state || "",
            status: paramsObj.status || "",
            minPrice: paramsObj.minPrice || "",
            maxPrice: paramsObj.maxPrice || "",
            ids: paramsObj.ids || "",
            address: paramsObj.address || "",
            zipcode: paramsObj.zipcode || "",
            propertyTypes: paramsObj.propertyTypes
                ? paramsObj.propertyTypes.split(",")
                : [],
            styles: paramsObj.styles ? paramsObj.styles.split(",") : [],
            yearBuiltMin: paramsObj.yearBuiltMin
                ? Number(paramsObj.yearBuiltMin)
                : undefined,
            yearBuiltMax: paramsObj.yearBuiltMax
                ? Number(paramsObj.yearBuiltMax)
                : undefined,
            glaMin: paramsObj.glaMin ? Number(paramsObj.glaMin) : undefined,
            glaMax: paramsObj.glaMax ? Number(paramsObj.glaMax) : undefined,
            basementSqFtMin: paramsObj.basementSqFtMin
                ? Number(paramsObj.basementSqFtMin)
                : undefined,
            basementSqFtMax: paramsObj.basementSqFtMax
                ? Number(paramsObj.basementSqFtMax)
                : undefined,
            basementFinished: parseBoolOrUndef(paramsObj.basementFinished),
            daysBackMin: paramsObj.daysBackMin
                ? Number(paramsObj.daysBackMin)
                : undefined,
            daysBackMax: paramsObj.daysBackMax
                ? Number(paramsObj.daysBackMax)
                : undefined,
        };

        setFilters(newFilters);
        setDidLoadFilters(true);
    }, [searchParams]);

    /* ----------------------------------------------------------
     *  2) Load polygons once
     * ---------------------------------------------------------- */
    async function loadPolygons() {
        try {
            const data = await fetchPolygons();
            setPolygons(data);

            // If we have a polygon selected, ensure it still exists
            if (selectedPolygon) {
                const stillExists = data.find((p) => p.id === selectedPolygon.id);
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

    // Listen for polygon changes from PolygonsList
    useEffect(() => {
        function handlePolygonDeleted() {
            loadPolygons();
        }

        function handlePolygonCreated() {
            loadPolygons();
        }

        window.addEventListener("polygonDeleted", handlePolygonDeleted);
        window.addEventListener("polygonCreated", handlePolygonCreated);
        return () => {
            window.removeEventListener("polygonDeleted", handlePolygonDeleted);
            window.removeEventListener("polygonCreated", handlePolygonCreated);
        };
    }, []);

    /* ----------------------------------------------------------
     *  3) Load map data (filtered + attached if polygon selected)
     * ---------------------------------------------------------- */
    useEffect(() => {
        if (!didLoadFilters) return;

        async function loadMapData() {
            setMapLoading(true);
            try {
                const polygonId = selectedPolygon ? selectedPolygon.id : undefined;
                const data = await fetchRealEstateMapData(filters, polygonId);
                setMapRealEstates(data);
            } catch (error) {
                console.error("Error loading map data:", error);
            } finally {
                setMapLoading(false);
            }
        }

        loadMapData();
    }, [filters, selectedPolygon, didLoadFilters]);

    /* ----------------------------------------------------------
     *  4) Load paginated list
     * ---------------------------------------------------------- */
    useEffect(() => {
        if (!didLoadFilters) return;

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
    }, [filters, listPage, listPageSize, didLoadFilters]);

    /* ----------------------------------------------------------
     *  5) handleFilterChange => rewrite URL
     * ---------------------------------------------------------- */
    function handleFilterChange(newFilters: RealEstateFilterParams) {
        const merged = {...filters, ...newFilters};
        const qp: Record<string, string> = {};

        if (merged.city) qp.city = merged.city;
        if (merged.state) qp.state = merged.state;
        if (merged.status) qp.status = merged.status;
        if (merged.minPrice) qp.minPrice = merged.minPrice;
        if (merged.maxPrice) qp.maxPrice = merged.maxPrice;
        if (merged.ids) qp.ids = merged.ids;
        if (merged.address) qp.address = merged.address;
        if (merged.zipcode) qp.zipcode = merged.zipcode;
        if (merged.propertyTypes && merged.propertyTypes.length > 0) {
            qp.propertyTypes = merged.propertyTypes.join(",");
        }
        if (merged.styles && merged.styles.length > 0) {
            qp.styles = merged.styles.join(",");
        }
        if (merged.yearBuiltMin !== undefined) {
            qp.yearBuiltMin = String(merged.yearBuiltMin);
        }
        if (merged.yearBuiltMax !== undefined) {
            qp.yearBuiltMax = String(merged.yearBuiltMax);
        }
        if (merged.glaMin !== undefined) {
            qp.glaMin = String(merged.glaMin);
        }
        if (merged.glaMax !== undefined) {
            qp.glaMax = String(merged.glaMax);
        }
        if (merged.basementSqFtMin !== undefined) {
            qp.basementSqFtMin = String(merged.basementSqFtMin);
        }
        if (merged.basementSqFtMax !== undefined) {
            qp.basementSqFtMax = String(merged.basementSqFtMax);
        }
        if (merged.basementFinished === true) {
            qp.basementFinished = "true";
        } else if (merged.basementFinished === false) {
            qp.basementFinished = "false";
        }
        if (merged.daysBackMin !== undefined) {
            qp.daysBackMin = String(merged.daysBackMin);
        }
        if (merged.daysBackMax !== undefined) {
            qp.daysBackMax = String(merged.daysBackMax);
        }

        const queryStr = new URLSearchParams(qp).toString();
        router.replace(`?${queryStr}`, {scroll: false});
    }

    /* ----------------------------------------------------------
     *  Polygon create + update
     * ---------------------------------------------------------- */
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
            alert("Polygon created successfully!");
            loadPolygons();
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
            loadPolygons();
            window.dispatchEvent(new Event("polygonCreated"));
            setEditMode(false);
        } catch (error) {
            console.error("Error updating polygon:", error);
            alert("Failed to update polygon.");
        }
    }

    /* ----------------------------------------------------------
     *  Build final array for the map
     *  BUGFIX: If "showAttached" is false, we *exclude*
     *  those attached items from the map entirely.
     * ---------------------------------------------------------- */
    let finalMapData: RealEstateMapDto[] = [];

    if (selectedPolygon) {
        // If not showing attached, remove those IDs from "filtered"
        let filteredWithoutAttached = mapRealEstates.filtered;
        if (!showAttached) {
            const attachedIdsSet = new Set(mapRealEstates.attached.map((a) => a.id));
            filteredWithoutAttached = filteredWithoutAttached.filter(
                (re) => !attachedIdsSet.has(re.id)
            );
        }

        if (showNotAttached) {
            finalMapData = [...finalMapData, ...filteredWithoutAttached];
        }
        if (showAttached) {
            finalMapData = [...finalMapData, ...mapRealEstates.attached];
        }
    } else {
        // No polygon selected => just show the filtered set
        finalMapData = mapRealEstates.filtered;
    }

    const paginationObj: PaginationDTO = {
        page: listPage,
        page_size: listPageSize,
        total_pages: listTotalPages,
        total_count: listTotalElements,
    };

    return (
        <div className="p-4 min-h-screen bg-gray-100">
            <h1 className="text-2xl font-bold mb-4">Real Estate Dashboard</h1>

            <RealEstateFilterForm filters={filters} onChange={handleFilterChange}/>

            <div className="flex flex-col lg:flex-row mt-4 gap-4">
                {/* LEFT: Paginated List */}
                <div className="flex-1">
                    <h2 className="text-xl font-semibold mb-2">
                        Properties List{" "}
                        {didLoadFilters ? (
                            <>
                                (showing {listRealEstates.length} of {listTotalElements})
                            </>
                        ) : (
                            "(loading...)"
                        )}
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

                        {/* Show/hide checkboxes */}
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
                            center={{lat: 40.114955, lng: -111.654923}}
                            zoom={11}
                            containerStyle={{width: "100%", height: "400px"}}
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
