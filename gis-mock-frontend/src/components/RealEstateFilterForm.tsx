// file: src/components/RealEstateFilterForm.tsx
"use client";

import React, {useEffect, useState} from "react";
import {RealEstateFilterParams} from "@/interfaces/RealEstateFilterParams";
import {useFilterOptions} from "@/hooks/useFilterOptions";
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
 *  - Basement Finished now has 3 states: All / Yes / No
 */
const RealEstateFilterForm: React.FC<Props> = ({filters, onChange}) => {
    const [localFilters, setLocalFilters] = useState<RealEstateFilterParams>(filters);

    // Keep local state in sync whenever "filters" prop changes
    useEffect(() => {
        setLocalFilters(filters);
    }, [filters]);

    // Single select filter options from the backend
    const {options: cityOptions, loading: cityLoading, error: cityError} = useFilterOptions("city");
    const {options: stateOptions, loading: stateLoading, error: stateError} = useFilterOptions("state");
    const {options: statusOptions, loading: statusLoading, error: statusError} = useFilterOptions("status");

    // Multi-select filter options
    const {options: propertyTypeOptions} = useFilterOptions("propertyType");
    const {options: styleOptions} = useFilterOptions("style");

    /**
     * Generic text/number change handler for the other fields
     */
    const handleInputChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
    ) => {
        const {name, value, type} = e.target;

        // For numeric fields, we can store the raw text as is (or parse to number).
        // But in the example code, we are storing them as strings in localFilters,
        // except for "yearBuiltMin" etc. Let's keep consistency:
        if (type === "checkbox") {
            // We used to do this for basementFinished, but now we have a select for it.
            // This is left as-is for any other checkboxes you might add in the future.
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
     * Special handler for the "Basement Finished" dropdown with 3 states:
     * - "all" → undefined
     * - "true" → true
     * - "false" → false
     */
    const handleBasementFinishedChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const selected = e.target.value;
        let boolVal: boolean | undefined = undefined;

        if (selected === "true") {
            boolVal = true;
        } else if (selected === "false") {
            boolVal = false;
        }
        // if "all", we keep boolVal = undefined

        setLocalFilters((prev) => ({
            ...prev,
            basementFinished: boolVal,
        }));
    };

    /**
     * For the TagMultiSelect fields (propertyTypes or styles).
     */
    const handleTagMultiSelectChange = (
        field: "propertyTypes" | "styles",
        newSelected: string[]
    ) => {
        setLocalFilters((prev) => ({
            ...prev,
            [field]: newSelected,
        }));
    };

    /**
     * When user clicks "Apply Filters"
     */
    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onChange(localFilters);
    };

    /**
     * Figure out which select value to show for basementFinished
     */
    const basementFinishedValue = localFilters.basementFinished === undefined
        ? "all"
        : localFilters.basementFinished
            ? "true"
            : "false";

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

                    {/* Basement Finished: select with 3 states */}
                    <div>
                        <label className="block text-sm font-medium">Basement Finished</label>
                        <select
                            name="basementFinished"
                            value={basementFinishedValue}
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
