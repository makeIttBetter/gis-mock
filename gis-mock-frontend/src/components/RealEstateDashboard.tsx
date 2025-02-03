// File: frontend/src/components/RealEstateDashboard.tsx
"use client";
import React, { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { RealEstate } from "@/interfaces/RealEstate";
import { fetchRealEstateData } from "@/lib/realEstateApi";
import RealEstateFilterForm, { RealEstateFilterParams } from "./RealEstateFilterForm";
import RealEstateList from "./RealEstateList";
import { RealEstateMap } from "@/components/map/RealEstateMap";
import PolygonsList from "./PolygonsList";

export default function RealEstateDashboard() {
    const router = useRouter();
    const searchParams = useSearchParams();

    const [filters, setFilters] = useState<RealEstateFilterParams>({
        city: "",
        state: "",
        status: "",
        minPrice: "",
        maxPrice: ""
    });
    const [realEstates, setRealEstates] = useState<RealEstate[]>([]);
    const [loading, setLoading] = useState<boolean>(false);

    // Parse URL search parameters into filters
    useEffect(() => {
        if (!searchParams) return;
        const params = Object.fromEntries(searchParams.entries());
        setFilters({
            city: params.city || "",
            state: params.state || "",
            status: params.status || "",
            minPrice: params.minPrice || "",
            maxPrice: params.maxPrice || ""
        });
    }, [searchParams]);

    // Fetch real estate data when filters change
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

    // Handler for filter changes: update URL with new query parameters (using scroll: false)
    const handleFilterChange = (newFilters: RealEstateFilterParams) => {
        const merged = { ...filters, ...newFilters };
        const query = new URLSearchParams(merged as Record<string, string>).toString();
        router.replace(`?${query}`, { scroll: false });
    };

    return (
        <div className="p-4 min-h-screen bg-gray-100">
            <h1 className="text-2xl font-bold mb-4">Real Estate Dashboard</h1>
            {/* Filter Form at the top */}
            <RealEstateFilterForm filters={filters} onChange={handleFilterChange} />
            <div className="flex flex-col lg:flex-row mt-4 gap-4">
                {/* Left side: Real Estate List */}
                <div className="flex-1">
                    <h2 className="text-xl font-semibold mb-2">Properties List</h2>
                    {loading ? (
                        <div>Loading properties...</div>
                    ) : (
                        <RealEstateList realEstates={realEstates} />
                    )}
                </div>
                {/* Right side: Map and Polygons List */}
                <div className="flex-1 flex flex-col gap-4">
                    <div
                        className="border p-2"
                        style={{ resize: 'horizontal', overflow: 'auto', minWidth: '300px' }}
                    >
                        <h2 className="text-xl font-semibold mb-2">Map View</h2>
                        <RealEstateMap
                            realEstates={realEstates}
                            center={{ lat: 40.114955, lng: -111.654923 }}
                            zoom={11}
                            containerStyle={{ width: "100%", height: "400px" }}
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
