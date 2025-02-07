"use client";

import React, { useEffect, useState } from "react";
import { RealEstateFilterParams } from "@/interfaces/RealEstateFilterParams";
import { useFilterOptions } from "@/hooks/useFilterOptions";
import TagMultiSelect from "@/components/TagMultiSelect";

interface Props {
    filters: RealEstateFilterParams;
    onChange: (newFilters: RealEstateFilterParams) => void;
}

/**
 * A form for filtering real estate data.
 * Features:
 *  - Single-select fields for city, state, status
 *  - Tag-based multi-select for property type and style
 *  - Basic numeric fields for price, year, GLA, etc.
 */
const RealEstateFilterForm: React.FC<Props> = ({ filters, onChange }) => {
    const [localFilters, setLocalFilters] = useState<RealEstateFilterParams>(filters);

    // Keep local state in sync whenever "filters" prop changes
    useEffect(() => {
        setLocalFilters(filters);
    }, [filters]);

    // Single select filter options
    const { options: cityOptions, loading: cityLoading, error: cityError } = useFilterOptions("city");
    const { options: stateOptions, loading: stateLoading, error: stateError } = useFilterOptions("state");
    const { options: statusOptions, loading: statusLoading, error: statusError } = useFilterOptions("status");

    // Multi-select filter options
    const { options: propertyTypeOptions } = useFilterOptions("propertyType");
    const { options: styleOptions } = useFilterOptions("style");

    /**
     * Fired when text/number/checkbox changes in normal fields
     */
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value, type } = e.target;
        if (type === "checkbox") {
            setLocalFilters((prev) => ({
                ...prev,
                [name]: (e.target as HTMLInputElement).checked,
            }));
        } else {
            setLocalFilters((prev) => ({
                ...prev,
                [name]: value,
            }));
        }
    };

    /**
     * For the TagMultiSelect fields (propertyTypes or styles).
     */
    const handleTagMultiSelectChange = (field: "propertyTypes" | "styles", newSelected: string[]) => {
        setLocalFilters((prev) => ({
            ...prev,
            [field]: newSelected,
        }));
    };

    // When user clicks "Apply Filters"
    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onChange(localFilters);
    };

    return (
        <form onSubmit={handleSubmit} className="bg-white p-4 rounded shadow space-y-4">
            {/* Group 1: Location Filters */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                    <h3 className="text-lg font-semibold">Location Filters</h3>

                    {/* City */}
                    <div>
                        <label className="block text-sm font-medium">City</label>
                        {cityLoading ? (
                            <div className="text-gray-500 text-sm">Loading city options...</div>
                        ) : cityError ? (
                            <div className="text-red-500 text-sm">Error loading: {cityError}</div>
                        ) : (
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
                        )}
                    </div>

                    {/* State */}
                    <div>
                        <label className="block text-sm font-medium">State</label>
                        {stateLoading ? (
                            <div className="text-gray-500 text-sm">Loading state options...</div>
                        ) : stateError ? (
                            <div className="text-red-500 text-sm">Error loading: {stateError}</div>
                        ) : (
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
                        )}
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

                {/* Group 2: Status & Price */}
                <div className="space-y-2">
                    <h3 className="text-lg font-semibold">Status & Price</h3>

                    {/* Status */}
                    <div>
                        <label className="block text-sm font-medium">Status</label>
                        {statusLoading ? (
                            <div className="text-gray-500 text-sm">Loading status options...</div>
                        ) : statusError ? (
                            <div className="text-red-500 text-sm">Error loading: {statusError}</div>
                        ) : (
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
                        )}
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

            {/* Group 3: Property Details & Additional Filters */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Property Types / Style */}
                <div className="space-y-2">
                    <h3 className="text-lg font-semibold">Property Details</h3>

                    {/* Property Type (Tag Multi-select) */}
                    <div>
                        <label className="block text-sm font-medium mb-1">Property Type</label>
                        <TagMultiSelect
                            availableOptions={propertyTypeOptions}
                            selectedValues={localFilters.propertyTypes || []}
                            onChange={(vals) => handleTagMultiSelectChange("propertyTypes", vals)}
                            placeholder="Select property types..."
                        />
                    </div>

                    <br/>
                    <br/>

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

                {/* Additional Filters */}
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

                    <div className="flex items-center">
                        <input
                            type="checkbox"
                            name="basementFinished"
                            checked={localFilters.basementFinished || false}
                            onChange={handleInputChange}
                            className="mr-2"
                        />
                        <label className="text-sm font-medium">Basement Finished</label>
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

            {/* Submit button */}
            <button
                type="submit"
                className="mt-4 px-4 py-2 bg-blue-600 text-white rounded"
            >
                Apply Filters
            </button>
        </form>
    );
};

export default RealEstateFilterForm;
