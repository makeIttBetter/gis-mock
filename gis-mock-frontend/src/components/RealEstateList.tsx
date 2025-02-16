"use client";
import React from "react";
import { RealEstate } from "@/interfaces/RealEstate";

/**
 * Props:
 *  realEstates: The array of real estate items to display in a list.
 */
interface Props {
    realEstates: RealEstate[];
}

/**
 * Displays a simple list of real estate objects,
 * now showing MLS# instead of the internal ID.
 */
const RealEstateList: React.FC<Props> = ({ realEstates }) => {
    return (
        <div className="bg-white p-4 rounded shadow">
            {realEstates.length === 0 ? (
                <div>No properties found.</div>
            ) : (
                <ul className="divide-y">
                    {realEstates.map((re) => (
                        <li key={re.id} className="py-2">
                            {/* Instead of "ID: re.id", show "MLS#: re.mlsNumber" */}
                            <div className="text-xs text-gray-500">MLS#: {re.mlsNumber}</div>

                            {/* Address info */}
                            <div className="font-semibold">{re.address}</div>
                            <div className="text-sm text-gray-600">
                                {re.city}, {re.state} {re.zip}
                            </div>

                            {/* Status & price info */}
                            <div className="text-sm">
                                Status: {re.status} | Price: {re.listPrice}
                            </div>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default RealEstateList;
