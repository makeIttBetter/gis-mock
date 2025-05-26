"use client";

import React from "react";
import {RealEstate} from "@/interfaces/RealEstate";
import {PaginationDTO} from "@/interfaces/PaginationDTO";
import RealEstateList from "@/components/RealEstateList";
import {PaginationControls} from "@/components/PaginationControls";

interface RealEstateListPanelProps {
    listRealEstates: RealEstate[];
    listLoading: boolean;
    pagination: PaginationDTO;
    onPageChange: (newPage: number) => void;
}

export default function RealEstateListPanel({
                                                listRealEstates,
                                                listLoading,
                                                pagination,
                                                onPageChange,
                                            }: RealEstateListPanelProps) {
    return (
        <div>
            <h2 className="text-xl font-semibold mb-2">
                Properties List (showing {listRealEstates.length} of {pagination.total_count})
            </h2>

            {listLoading ? (
                <div>Loading properties...</div>
            ) : (
                <RealEstateList realEstates={listRealEstates}/>
            )}

            <PaginationControls pagination={pagination} onPageChange={onPageChange}/>
        </div>
    );
}
