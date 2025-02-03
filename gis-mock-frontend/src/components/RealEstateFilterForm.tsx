// File: frontend/src/components/RealEstateFilterForm.tsx
"use client";
import React, { useState } from "react";

export interface RealEstateFilterParams {
    city: string;
    state: string;
    status: string;
    minPrice: string;
    maxPrice: string;
}

interface Props {
    filters: RealEstateFilterParams;
    onChange: (newFilters: RealEstateFilterParams) => void;
}

const RealEstateFilterForm: React.FC<Props> = ({ filters, onChange }) => {
    const [localFilters, setLocalFilters] = useState<RealEstateFilterParams>(filters);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setLocalFilters(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onChange(localFilters);
    };

    return (
        <form onSubmit={handleSubmit} className="bg-white p-4 rounded shadow mb-4">
            <div className="flex flex-wrap gap-4">
                <div>
                    <label className="block text-sm font-medium">City</label>
                    <input
                        type="text"
                        name="city"
                        value={localFilters.city}
                        onChange={handleInputChange}
                        className="border rounded p-1"
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium">State</label>
                    <input
                        type="text"
                        name="state"
                        value={localFilters.state}
                        onChange={handleInputChange}
                        className="border rounded p-1"
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium">Status</label>
                    <select
                        name="status"
                        value={localFilters.status}
                        onChange={handleInputChange}
                        className="border rounded p-1"
                    >
                        <option value="">Any</option>
                        <option value="SOLD">SOLD</option>
                        <option value="AVAILABLE">AVAILABLE</option>
                        {/* Add more statuses as needed */}
                    </select>
                </div>
                <div>
                    <label className="block text-sm font-medium">Min Price</label>
                    <input
                        type="number"
                        name="minPrice"
                        value={localFilters.minPrice}
                        onChange={handleInputChange}
                        className="border rounded p-1"
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium">Max Price</label>
                    <input
                        type="number"
                        name="maxPrice"
                        value={localFilters.maxPrice}
                        onChange={handleInputChange}
                        className="border rounded p-1"
                    />
                </div>
            </div>
            <button type="submit" className="mt-4 px-4 py-2 bg-blue-600 text-white rounded">
                Apply Filters
            </button>
        </form>
    );
};

export default RealEstateFilterForm;
