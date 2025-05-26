"use client";
import React from "react";
import {PaginationDTO} from "@/interfaces/PaginationDTO";

interface Props {
    pagination: PaginationDTO;
    onPageChange: (newPage: number) => void;
}

export const PaginationControls: React.FC<Props> = ({
                                                        pagination,
                                                        onPageChange
                                                    }) => {
    const {page, page_size, total_pages, total_count} = pagination;

    function handlePrev() {
        if (page > 1) onPageChange(page - 1);
    }

    function handleNext() {
        if (page < total_pages) onPageChange(page + 1);
    }

    return (
        <div className="flex items-center gap-4 mt-4">
            <button
                onClick={handlePrev}
                disabled={page <= 1}
                className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50"
            >
                Prev
            </button>
            <div>
                Page <strong>{page}</strong> of <strong>{total_pages}</strong>
                <span className="ml-2 text-sm text-gray-600">
          (Total Items: {total_count}, Page Size: {page_size})
        </span>
            </div>
            <button
                onClick={handleNext}
                disabled={page >= total_pages}
                className="px-4 py-2 bg-gray-300 rounded disabled:opacity-50"
            >
                Next
            </button>
        </div>
    );
};
