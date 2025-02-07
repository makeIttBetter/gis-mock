"use client";

import { useState, useEffect } from "react";
import { apiGetPath } from "@/lib/api";

/**
 * Hook for fetching distinct filter options for a given property key
 * (e.g. "city", "state", "status", "propertyType", "style").
 *
 * To prevent infinite loops, we run the fetch exactly once whenever
 * the 'property' value changes, then store it in state. This ensures
 * we don't call setState repeatedly inside an effect.
 */
export function useFilterOptions(property: string) {
    const [options, setOptions] = useState<string[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        // If property is empty, skip
        if (!property) return;

        let isMounted = true; // To prevent state updates if unmounted
        const fetchData = async () => {
            setLoading(true);
            setError(null);
            try {
                // e.g. GET /api/real-estate/filters/{property}
                const data = await apiGetPath<string[]>("REAL_ESTATE_FILTERS", property);
                if (isMounted) {
                    setOptions(data);
                }
            } catch (err: any) {
                if (isMounted) {
                    setError(err.message || "Error fetching filter options");
                }
            } finally {
                if (isMounted) {
                    setLoading(false);
                }
            }
        };

        fetchData();

        return () => {
            isMounted = false;
        };
    }, [property]);

    return {
        options,
        loading,
        error,
    };
}
