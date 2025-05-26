// File: src/components/polygons/edit/PolygonMapPanel.tsx
/* eslint-disable react-hooks/exhaustive-deps */
"use client";

import React, {useCallback, useEffect, useRef, useState} from "react";
import {GoogleMap, InfoWindow, Polygon as MapPolygon, useLoadScript,} from "@react-google-maps/api";
import {PolygonDTO} from "@/interfaces/PolygonDTO";
import {RealEstateMapDto} from "@/interfaces/RealEstateMapDto";
import RealEstateMarkerInfo from "@/components/realestate/RealEstateMarkerInfo";
import {RealEstateMarkerInfoMode} from "@/components/realestate/RealEstateMarkerInfoMode";
import {RealEstateFilterParams} from "@/interfaces/RealEstateFilterParams";
import debounce from "lodash.debounce";

/* ------------------------------------------------------------------ */
/* Persist camera centre across un-mounts                             */
/* ------------------------------------------------------------------ */
let globalCenter: google.maps.LatLngLiteral | null = null;

/* ------------------------------------------------------------------ */
/* Component props                                                    */

/* ------------------------------------------------------------------ */
interface PolygonMapPanelProps {
    polygon: PolygonDTO;
    polygonName: string;
    displayedRealEstates: RealEstateMapDto[];
    attachedSet: Set<string>;
    onToggleAttachment: (marker: RealEstateMapDto) => void;

    showNotAttached: boolean;
    setShowNotAttached: (v: boolean) => void;
    showAttached: boolean;
    setShowAttached: (v: boolean) => void;

    onSavePolygon: (coords?: { lat: number; lng: number }[]) => void;

    filters: RealEstateFilterParams; // kept for future use
    setFilters: (f: RealEstateFilterParams) => void;

    isUpdating: boolean;
}

/* ------------------------------------------------------------------ */
/* Main component                                                     */
/* ------------------------------------------------------------------ */
export default function PolygonMapPanel({
                                            polygon,
                                            polygonName,
                                            displayedRealEstates,
                                            attachedSet,
                                            onToggleAttachment,

                                            showNotAttached,
                                            setShowNotAttached,
                                            showAttached,
                                            setShowAttached,

                                            onSavePolygon,
                                            isUpdating,
                                        }: PolygonMapPanelProps) {
    const {isLoaded} = useLoadScript({
        googleMapsApiKey: process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || "",
        libraries: ["drawing", "geometry"],
    });

    /* ---------------- refs ---------------- */
    const mapRef = useRef<google.maps.Map | null>(null);
    const polygonRef = useRef<google.maps.Polygon | null>(null);
    const markerPool = useRef<google.maps.Marker[]>([]);

    /* ---------------- state --------------- */
    const [selected, setSelected] = useState<RealEstateMapDto | null>(null);
    const [mapCenter, setMapCenter] = useState<google.maps.LatLngLiteral>(() => {
        if (globalCenter) return globalCenter;
        const path = polygon.coordinates ?? [];
        return path.length ? {lat: path[0].lat, lng: path[0].lng} : {lat: 40.114955, lng: -111.654923};
    });

    /* ------------------------------------------------------------------ */
    /* Marker pool – only what is visible                                 */
    /* ------------------------------------------------------------------ */
    const rebuildMarkers = useCallback(() => {
        const map = mapRef.current;
        if (!map) return;
        const bounds = map.getBounds();
        if (!bounds) return;

        // Clear previous markers
        markerPool.current.forEach((m) => m.setMap(null));
        markerPool.current = [];

        displayedRealEstates.forEach((re) => {
            if (
                re.latitude == null ||
                re.longitude == null ||
                !bounds.contains(new google.maps.LatLng(re.latitude, re.longitude))
            )
                return;

            const marker = new google.maps.Marker({
                position: {lat: re.latitude, lng: re.longitude},
                map,
                icon: {
                    url: attachedSet.has(re.id) ? "/yellow-dot.png" : "/red-dot.png",
                },
                title: re.mlsNumber ?? "",
            });
            marker.addListener("click", () => setSelected(re));
            markerPool.current.push(marker);
        });
    }, [displayedRealEstates, attachedSet]);

    const rebuildMarkersDebounced = useCallback(debounce(rebuildMarkers, 250), [
        rebuildMarkers,
    ]);

    useEffect(() => {
        if (isLoaded && mapRef.current) rebuildMarkers();
    }, [isLoaded, rebuildMarkers]);

    /* ------------------------------------------------------------------ */
    /* Helpers                                                            */
    /* ------------------------------------------------------------------ */
    const readPolygonCoords = () => {
        if (!polygonRef.current) return undefined;
        const path = polygonRef.current.getPath();
        if (path.getLength() < 3) return undefined;
        const coords: { lat: number; lng: number }[] = [];
        for (let i = 0; i < path.getLength(); i++) {
            const pt = path.getAt(i);
            coords.push({lat: pt.lat(), lng: pt.lng()});
        }
        return coords;
    };

    const handleSave = () => {
        onSavePolygon(readPolygonCoords());
    };

    /* ------------------------------------------------------------------ */
    /* Render                                                             */
    /* ------------------------------------------------------------------ */
    if (!isLoaded) return <div>Loading Google Maps...</div>;

    const path = polygon.coordinates ?? [];

    return (
        <div className="bg-white p-4 rounded shadow">
            <h2 className="text-lg font-semibold mb-2">Polygon Editor: {polygonName}</h2>

            {/* toggles */}
            <div className="flex gap-4 mb-2">
                <label className="flex items-center">
                    <input
                        type="checkbox"
                        checked={showNotAttached}
                        onChange={(e) => setShowNotAttached(e.target.checked)}
                    />
                    <span className="ml-1 text-sm">Show Not-Attached</span>
                </label>
                <label className="flex items-center">
                    <input
                        type="checkbox"
                        checked={showAttached}
                        onChange={(e) => setShowAttached(e.target.checked)}
                    />
                    <span className="ml-1 text-sm">Show Attached</span>
                </label>
            </div>

            {/* map ---------------------------------------------------------- */}
            <div style={{width: "100%", height: 500}} className="border rounded overflow-hidden">
                <GoogleMap
                    mapContainerStyle={{width: "100%", height: "100%"}}
                    center={mapCenter}
                    zoom={10}
                    onLoad={(m) => {
                        mapRef.current = m; // returns void
                    }}
                    onIdle={() => {
                        const c = mapRef.current?.getCenter();
                        if (c) {
                            const nc = {lat: c.lat(), lng: c.lng()};
                            setMapCenter(nc);
                            globalCenter = nc;
                        }
                        rebuildMarkersDebounced();
                    }}
                >
                    {path.length > 2 && (
                        <MapPolygon
                            paths={path}
                            editable
                            onLoad={(poly) => (polygonRef.current = poly)}
                            options={{
                                fillColor: "#00FF00",
                                fillOpacity: 0.3,
                                strokeColor: "#00FF00",
                                strokeWeight: 2,
                            }}
                        />
                    )}

                    {/* InfoWindow */}
                    {selected && selected.latitude != null && selected.longitude != null && (
                        <InfoWindow
                            position={{lat: selected.latitude, lng: selected.longitude}}
                            onCloseClick={() => setSelected(null)}
                        >
                            <RealEstateMarkerInfo
                                realEstate={{
                                    id: selected.id,
                                    mlsNumber: selected.mlsNumber ?? "",
                                    soldTerms: selected.soldTerms ?? "",
                                    soldPrice: selected.soldPrice ?? "",
                                    taxId: selected.taxId ?? "",
                                    address: selected.address ?? "",
                                    city: selected.city ?? "",
                                    state: selected.state ?? "",
                                    zip: selected.zip ?? "",
                                    status: selected.status ?? "",
                                    listPrice: "",
                                    latitude: String(selected.latitude),
                                    longitude: String(selected.longitude),
                                }}
                                isAttached={attachedSet.has(selected.id)}
                                onToggleAttachment={() => {
                                    onToggleAttachment(selected);
                                    setSelected(null);
                                }}
                                onClose={() => setSelected(null)}
                                mode={RealEstateMarkerInfoMode.POLYGON_EDIT_PAGE}
                            />
                        </InfoWindow>
                    )}
                </GoogleMap>
            </div>

            {/* save button -------------------------------------------------- */}
            <div className="flex justify-center mt-4">
                <button
                    onClick={handleSave}
                    disabled={isUpdating}
                    className="px-6 py-3 bg-blue-600 text-white text-lg rounded hover:bg-blue-700"
                >
                    {isUpdating ? "Saving…" : "Save Changes to Polygon"}
                </button>
            </div>
        </div>
    );
}
