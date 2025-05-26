"use client";

import React, {useEffect, useState} from "react";
import {RealEstateFilterParams} from "@/interfaces/RealEstateFilterParams";
import {useFilterOptions} from "@/hooks/useFilterOptions";
import TagMultiSelect from "@/components/TagMultiSelect";

interface Props {
    filters: RealEstateFilterParams;
    onChange: (newFilters: RealEstateFilterParams) => void;
}

export default function RealEstateFilterPanel({filters, onChange}: Props) {
    const [localFilters, setLocalFilters] = useState<RealEstateFilterParams>(filters);

    // Keep local state in sync
    useEffect(() => {
        setLocalFilters(filters);
    }, [filters]);

    // Single-select filter options
    const {options: cityOptions} = useFilterOptions("city");
    const {options: stateOptions} = useFilterOptions("state");
    const {options: statusOptions} = useFilterOptions("status");

    // Basic change handler
    const handleInputChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
    ) => {
        const {name, value} = e.target;
        setLocalFilters((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    // 3-state select for basementFinished
    const handleBasementFinishedChange = (
        e: React.ChangeEvent<HTMLSelectElement>
    ) => {
        let boolVal: boolean | undefined;
        if (e.target.value === "true") boolVal = true;
        else if (e.target.value === "false") boolVal = false;
        else boolVal = undefined;
        setLocalFilters((prev) => ({
            ...prev,
            basementFinished: boolVal,
        }));
    };

    const handleTagMultiSelectChange = (
        field: "propertyTypes" | "styles",
        newValues: string[]
    ) => {
        setLocalFilters((prev) => ({
            ...prev,
            [field]: newValues,
        }));
    };

    // Submit => calls onChange
    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onChange(localFilters);
    };

    // Derive the dropdown value
    const basementValue =
        localFilters.basementFinished === undefined
            ? "all"
            : localFilters.basementFinished
                ? "true"
                : "false";

    return (
        <form onSubmit={handleSubmit} className="bg-white p-4 rounded shadow space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Location Filters */}
                <div className="space-y-2">
                    <h3 className="text-lg font-semibold">Location Filters</h3>

                    {/* City */}
                    <div>
                        <label className="block text-sm font-medium">City</label>
                        <select
                            name="city"
                            value={localFilters.city || ""}
                            onChange={handleInputChange}
                            className="border rounded p-1 w-full"
                        >
                            <option value="">All</option>
                            {cityOptions.map((opt) => (
                                <option key={opt} value={opt}>
                                    {opt}
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* State */}
                    <div>
                        <label className="block text-sm font-medium">State</label>
                        <select
                            name="state"
                            value={localFilters.state || ""}
                            onChange={handleInputChange}
                            className="border rounded p-1 w-full"
                        >
                            <option value="">All</option>
                            {stateOptions.map((opt) => (
                                <option key={opt} value={opt}>
                                    {opt}
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* Zipcode */}
                    <div>
                        <label className="block text-sm font-medium">Zipcode</label>
                        <input
                            type="text"
                            name="zipcode"
                            value={localFilters.zipcode || ""}
                            onChange={handleInputChange}
                            className="border rounded p-1 w-full"
                        />
                    </div>
                </div>

                {/* Status & Price */}
                <div className="space-y-2">
                    <h3 className="text-lg font-semibold">Status & Price</h3>

                    {/* Status */}
                    <div>
                        <label className="block text-sm font-medium">Status</label>
                        <select
                            name="status"
                            value={localFilters.status || ""}
                            onChange={handleInputChange}
                            className="border rounded p-1 w-full"
                        >
                            <option value="">All</option>
                            {statusOptions.map((opt) => (
                                <option key={opt} value={opt}>
                                    {opt}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label className="block text-sm font-medium">Min Price</label>
                        <input
                            type="number"
                            name="minPrice"
                            value={localFilters.minPrice || ""}
                            onChange={handleInputChange}
                            className="border rounded p-1 w-full"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium">Max Price</label>
                        <input
                            type="number"
                            name="maxPrice"
                            value={localFilters.maxPrice || ""}
                            onChange={handleInputChange}
                            className="border rounded p-1 w-full"
                        />
                    </div>
                </div>
            </div>

            {/* Property + Additional */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                    <h3 className="text-lg font-semibold">Property Details</h3>

                    {/* Property Types */}
                    <div>
                        <label className="block text-sm font-medium mb-1">Property Type</label>
                        <TagMultiSelect
                            availableOptions={[] /* loaded from backend? */}
                            selectedValues={localFilters.propertyTypes || []}
                            onChange={(vals) => handleTagMultiSelectChange("propertyTypes", vals)}
                            placeholder="Select property types..."
                        />
                    </div>

                    {/* Year Built */}
                    <div className="flex space-x-2">
                        <div className="w-1/2">
                            <label className="block text-sm font-medium">Year Built (Min)</label>
                            <input
                                type="number"
                                name="yearBuiltMin"
                                value={localFilters.yearBuiltMin ?? ""}
                                onChange={handleInputChange}
                                className="border rounded p-1 w-full"
                            />
                        </div>
                        <div className="w-1/2">
                            <label className="block text-sm font-medium">Year Built (Max)</label>
                            <input
                                type="number"
                                name="yearBuiltMax"
                                value={localFilters.yearBuiltMax ?? ""}
                                onChange={handleInputChange}
                                className="border rounded p-1 w-full"
                            />
                        </div>
                    </div>
                </div>

                <div className="space-y-2">
                    <h3 className="text-lg font-semibold">Additional Filters</h3>

                    <div className="flex space-x-2">
                        <div className="w-1/2">
                            <label className="block text-sm font-medium">GLA (Min)</label>
                            <input
                                type="number"
                                name="glaMin"
                                value={localFilters.glaMin ?? ""}
                                onChange={handleInputChange}
                                className="border rounded p-1 w-full"
                            />
                        </div>
                        <div className="w-1/2">
                            <label className="block text-sm font-medium">GLA (Max)</label>
                            <input
                                type="number"
                                name="glaMax"
                                value={localFilters.glaMax ?? ""}
                                onChange={handleInputChange}
                                className="border rounded p-1 w-full"
                            />
                        </div>
                    </div>

                    <div className="flex space-x-2">
                        <div className="w-1/2">
                            <label className="block text-sm font-medium">Basement Sq Ft (Min)</label>
                            <input
                                type="number"
                                name="basementSqFtMin"
                                value={localFilters.basementSqFtMin ?? ""}
                                onChange={handleInputChange}
                                className="border rounded p-1 w-full"
                            />
                        </div>
                        <div className="w-1/2">
                            <label className="block text-sm font-medium">Basement Sq Ft (Max)</label>
                            <input
                                type="number"
                                name="basementSqFtMax"
                                value={localFilters.basementSqFtMax ?? ""}
                                onChange={handleInputChange}
                                className="border rounded p-1 w-full"
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-medium">Basement Finished</label>
                        <select
                            value={basementValue}
                            onChange={handleBasementFinishedChange}
                            className="border rounded p-1 w-full"
                        >
                            <option value="all">All</option>
                            <option value="true">Yes</option>
                            <option value="false">No</option>
                        </select>
                    </div>

                    <div className="flex space-x-2">
                        <div className="w-1/2">
                            <label className="block text-sm font-medium">Days Back (Min)</label>
                            <input
                                type="number"
                                name="daysBackMin"
                                value={localFilters.daysBackMin ?? ""}
                                onChange={handleInputChange}
                                className="border rounded p-1 w-full"
                            />
                        </div>
                        <div className="w-1/2">
                            <label className="block text-sm font-medium">Days Back (Max)</label>
                            <input
                                type="number"
                                name="daysBackMax"
                                value={localFilters.daysBackMax ?? ""}
                                onChange={handleInputChange}
                                className="border rounded p-1 w-full"
                            />
                        </div>
                    </div>
                </div>
            </div>

            <button
                type="submit"
                className="mt-4 px-4 py-2 bg-blue-600 text-white rounded"
            >
                Apply Filters
            </button>
        </form>
    );
}
