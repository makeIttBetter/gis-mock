"use client";

import React, { useEffect, useState, useCallback } from "react";
import { fetchPolygonById, updatePolygon } from "@/lib/polygonApi";
import { fetchRealEstateMapData } from "@/lib/realEstateApi";
import { PolygonDTO } from "@/interfaces/PolygonDTO";
import { RealEstateMapDto } from "@/interfaces/RealEstateMapDto";
import { RealEstateFilterParams } from "@/interfaces/RealEstateFilterParams";

import PolygonInfoForm from "./PolygonInfoForm";
import PolygonMapPanel from "./PolygonMapPanel";

/**
 * Container that loads a polygon by ID, fetches real estate, and
 * renders the polygon + real estate toggles. Replaces old `PolygonEditor`.
 */
interface PolygonEditorContainerProps {
    polygonId: string;
}

export default function PolygonEditorContainer({
                                                   polygonId,
                                               }: PolygonEditorContainerProps) {
    const [polygon, setPolygon] = useState<PolygonDTO | null>(null);
    const [polygonName, setPolygonName] = useState<string>("");

    const [filters, setFilters] = useState<RealEstateFilterParams>({});
    const [allRealEstates, setAllRealEstates] = useState<RealEstateMapDto[]>([]);
    const [attachedSet, setAttachedSet] = useState<Set<string>>(new Set());

    const [showNotAttached, setShowNotAttached] = useState(true);
    const [showAttached, setShowAttached] = useState(true);
    const [isUpdating, setIsUpdating] = useState(false);

    // Load polygon
    const loadPolygon = useCallback(async () => {
        try {
            const p = await fetchPolygonById(polygonId);
            if (!p) {
                alert("Polygon not found or not accessible.");
                return;
            }
            setPolygon(p);
            setPolygonName(p.name || "");
            setAttachedSet(new Set(p.realEstateObjects || []));
        } catch (err) {
            console.error("Error loading polygon data:", err);
            alert("Failed to load polygon data.");
        }
    }, [polygonId]);

    // Load real estate
    const loadRealEstates = useCallback(async () => {
        try {
            // we fetch both: filtered + attached
            const data = await fetchRealEstateMapData(filters, polygonId);
            // combine them into a single array
            const combinedMap = new Map<string, RealEstateMapDto>();
            [...data.filtered, ...data.attached].forEach((re) =>
                combinedMap.set(re.id, re)
            );
            setAllRealEstates(Array.from(combinedMap.values()));
        } catch (error) {
            console.error("Error loading real estate map data:", error);
            alert("Failed to load real estate map data.");
        }
    }, [filters, polygonId]);

    useEffect(() => {
        loadPolygon();
    }, [loadPolygon]);

    useEffect(() => {
        loadRealEstates();
    }, [loadRealEstates]);

    // Save polygon changes
    async function handleSavePolygonChanges(newCoordinates?: { lat: number; lng: number }[]) {
        if (!polygon) return;
        setIsUpdating(true);

        try {
            // If user manually changed the shape, we use newCoordinates
            let coords = polygon.coordinates;
            if (newCoordinates && newCoordinates.length > 2) {
                coords = newCoordinates;
            }

            const reIds = Array.from(attachedSet);

            const updated = await updatePolygon(polygon.id, {
                name: polygonName.trim(),
                coordinates: coords,
                realEstateIds: reIds,
            });

            alert("Polygon updated successfully!");
            setPolygon(updated);
            setPolygonName(updated.name || "");
            setAttachedSet(new Set(updated.realEstateObjects || []));
        } catch (err) {
            console.error("Error updating polygon:", err);
            alert("Failed to update polygon.");
        } finally {
            setIsUpdating(false);
        }
    }

    // Toggle attach/detach
    function handleToggleAttachment(marker: RealEstateMapDto) {
        const newSet = new Set(attachedSet);
        if (newSet.has(marker.id)) newSet.delete(marker.id);
        else newSet.add(marker.id);
        setAttachedSet(newSet);
    }

    // Which real estates to display
    const displayedRealEstates = allRealEstates.filter((re) => {
        const isAttached = attachedSet.has(re.id);
        if (isAttached && !showAttached) return false;
        if (!isAttached && !showNotAttached) return false;
        return true;
    });

    return (
        <div className="space-y-4">
            {polygon ? (
                <>
                    <PolygonInfoForm
                        polygonName={polygonName}
                        setPolygonName={setPolygonName}
                    />

                    {/* Here you can place your "RealEstateFilterForm" if you want a filter panel */}
                    {/* Or keep it as the old code if you want. We'll skip for brevity. */}

                    <PolygonMapPanel
                        polygon={polygon}
                        polygonName={polygonName}
                        displayedRealEstates={displayedRealEstates}
                        attachedSet={attachedSet}
                        onToggleAttachment={handleToggleAttachment}
                        showNotAttached={showNotAttached}
                        setShowNotAttached={setShowNotAttached}
                        showAttached={showAttached}
                        setShowAttached={setShowAttached}
                        onSavePolygon={handleSavePolygonChanges}
                        filters={filters}
                        setFilters={setFilters}
                        isUpdating={isUpdating}
                    />
                </>
            ) : (
                <div>Loading polygon...</div>
            )}
        </div>
    );
}
