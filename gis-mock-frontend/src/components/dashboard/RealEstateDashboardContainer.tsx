"use client";

import React, { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { RealEstateFilterParams } from "@/interfaces/RealEstateFilterParams";
import { PolygonDTO } from "@/interfaces/PolygonDTO";
import { RealEstateMapDto } from "@/interfaces/RealEstateMapDto";
import {
    fetchPolygons,
    createPolygon,
    updatePolygon,
} from "@/lib/polygonApi";
import {
    fetchRealEstateMapData,
    fetchRealEstatePaginated,
} from "@/lib/realEstateApi";
import { RealEstate } from "@/interfaces/RealEstate";
import { PaginationDTO } from "@/interfaces/PaginationDTO";

import RealEstateFilterPanel from "./RealEstateFilterPanel";
import RealEstateListPanel from "./RealEstateListPanel";
import MapPanel from "./MapPanel";
import PolygonListPanel from "./PolygonListPanel";

/**
 * The main container for your "/map" page. It:
 * - Reads filters from the URL search params
 * - Fetches polygons and real estate
 * - Passes data to sub-panels (Filter, List, Map, Polygons)
 */
export default function RealEstateDashboardContainer() {
    const router = useRouter();
    const searchParams = useSearchParams();

    /** Filters from the URL params */
    const [filters, setFilters] = useState<RealEstateFilterParams>({});
    const [didLoadFilters, setDidLoadFilters] = useState(false);

    /** Polygons list, plus currently selected polygon. */
    const [polygons, setPolygons] = useState<PolygonDTO[]>([]);
    const [selectedPolygon, setSelectedPolygon] = useState<PolygonDTO | null>(null);
    const [editMode, setEditMode] = useState(false);

    /** Real estate map data: filtered vs. attached to polygon. */
    const [mapRealEstates, setMapRealEstates] = useState<{
        filtered: RealEstateMapDto[];
        attached: RealEstateMapDto[];
    }>({ filtered: [], attached: [] });

    /** Paginated list data. */
    const [listRealEstates, setListRealEstates] = useState<RealEstate[]>([]);
    const [listLoading, setListLoading] = useState(false);
    const [listPage, setListPage] = useState<number>(1);
    const [listPageSize] = useState<number>(100);
    const [listTotalPages, setListTotalPages] = useState<number>(1);
    const [listTotalElements, setListTotalElements] = useState<number>(0);

    /** Toggles to show/hide attached or not-attached markers on the map. */
    const [showAttached, setShowAttached] = useState(true);
    const [showNotAttached, setShowNotAttached] = useState(true);

    /* ---------------------------
     * 1) Parse URL params -> filters
     --------------------------- */
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

    /* ---------------------------
     * 2) Load polygons
     --------------------------- */
    async function loadPolygons() {
        try {
            const data = await fetchPolygons();
            setPolygons(data);

            if (selectedPolygon) {
                // Check if the selected polygon still exists
                const exists = data.find((p) => p.id === selectedPolygon.id);
                if (!exists) {
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

    // Listen for polygon create/delete events
    useEffect(() => {
        function handlePolygonEvent() {
            loadPolygons();
        }
        window.addEventListener("polygonCreated", handlePolygonEvent);
        window.addEventListener("polygonDeleted", handlePolygonEvent);

        return () => {
            window.removeEventListener("polygonCreated", handlePolygonEvent);
            window.removeEventListener("polygonDeleted", handlePolygonEvent);
        };
    }, []);

    /* ---------------------------
     * 3) Load map data (filtered + attached if polygon selected)
     --------------------------- */
    useEffect(() => {
        if (!didLoadFilters) return;

        async function loadMapData() {
            try {
                const polygonId = selectedPolygon ? selectedPolygon.id : undefined;
                const data = await fetchRealEstateMapData(filters, polygonId);
                setMapRealEstates(data);
            } catch (error) {
                console.error("Error loading map data:", error);
            }
        }

        loadMapData();
    }, [filters, selectedPolygon, didLoadFilters]);

    /* ---------------------------
     * 4) Load paginated list
     --------------------------- */
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

    /* ---------------------------
     * 5) onChange Filter -> rewrite URL
     --------------------------- */
    function handleFilterChange(newFilters: RealEstateFilterParams) {
        const merged = { ...filters, ...newFilters };
        const qp: Record<string, string> = {};

        // Rebuild query parameters only for non-empty fields
        if (merged.city) qp.city = merged.city;
        if (merged.state) qp.state = merged.state;
        if (merged.status) qp.status = merged.status;
        if (merged.minPrice) qp.minPrice = merged.minPrice;
        if (merged.maxPrice) qp.maxPrice = merged.maxPrice;
        if (merged.ids) qp.ids = merged.ids;
        if (merged.address) qp.address = merged.address;
        if (merged.zipcode) qp.zipcode = merged.zipcode;
        if (merged.propertyTypes?.length) {
            qp.propertyTypes = merged.propertyTypes.join(",");
        }
        if (merged.styles?.length) {
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
        router.replace(`?${queryStr}`, { scroll: false });
    }

    /* ---------------------------
     * 6) Polygon create + update
     --------------------------- */
    async function handleCreatePolygon(payload: {
        name: string;
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) {
        try {
            const created = await createPolygon(
                payload.name,
                payload.coordinates,
                payload.realEstateIds
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

    async function handleUpdatePolygon(payload: {
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) {
        if (!selectedPolygon) return;
        try {
            const result = await updatePolygon(selectedPolygon.id, {
                name: selectedPolygon.name,
                coordinates: payload.coordinates,
                realEstateIds: payload.realEstateIds,
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

    /* ---------------------------
     * 7) Build final array for the map
     --------------------------- */
    let finalMapData: RealEstateMapDto[] = [];
    if (selectedPolygon) {
        // Remove attached from "filtered" if user wants to hide attached
        let filteredWithoutAttached = mapRealEstates.filtered;
        if (!showAttached) {
            const attachedIdsSet = new Set(mapRealEstates.attached.map((a) => a.id));
            filteredWithoutAttached = filteredWithoutAttached.filter(
                (re) => !attachedIdsSet.has(re.id)
            );
        }
        if (showNotAttached) {
            finalMapData = [...filteredWithoutAttached];
        }
        if (showAttached) {
            finalMapData = [...finalMapData, ...mapRealEstates.attached];
        }
    } else {
        // If no polygon selected => just show the filtered set
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

            {/* Filter Panel */}
            <RealEstateFilterPanel filters={filters} onChange={handleFilterChange} />

            {/* Main content area */}
            <div className="flex flex-col lg:flex-row mt-4 gap-4">
                {/* Left side: Real Estate List Panel */}
                <div className="flex-1">
                    <RealEstateListPanel
                        listRealEstates={listRealEstates}
                        listLoading={listLoading}
                        pagination={paginationObj}
                        onPageChange={setListPage}
                    />
                </div>

                {/* Right side: Map + Polygons */}
                <div className="flex-1 flex flex-col gap-4">
                    <MapPanel
                        polygons={polygons}
                        selectedPolygon={selectedPolygon}
                        editMode={editMode}
                        setSelectedPolygon={setSelectedPolygon}
                        setEditMode={setEditMode}
                        finalMapData={finalMapData}
                        showNotAttached={showNotAttached}
                        setShowNotAttached={setShowNotAttached}
                        showAttached={showAttached}
                        setShowAttached={setShowAttached}
                        onCreatePolygon={handleCreatePolygon}
                        onUpdatePolygon={handleUpdatePolygon}
                    />

                    <PolygonListPanel />
                </div>
            </div>
        </div>
    );
}
