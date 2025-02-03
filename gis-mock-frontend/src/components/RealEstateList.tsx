"use client";
import React from "react";
import { RealEstate } from "@/interfaces/RealEstate";

interface Props {
    realEstates: RealEstate[];
}

const RealEstateList: React.FC<Props> = ({ realEstates }) => {
    return (
        <div className="bg-white p-4 rounded shadow">
            {realEstates.length === 0 ? (
                <div>No properties found.</div>
            ) : (
                <ul className="divide-y">
                    {realEstates.map((re) => (
                        <li key={re.id} className="py-2">
                            <div className="text-xs text-gray-500">ID: {re.id}</div>
                            <div className="font-semibold">{re.address}</div>
                            <div className="text-sm text-gray-600">
                                {re.city}, {re.state} {re.zip}
                            </div>
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
