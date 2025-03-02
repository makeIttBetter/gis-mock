"use client";
import React, {useRef, useState} from "react";
import {
    GoogleMap,
    InfoWindow,
    Marker as AdvancedMarkerElement,
    Polygon as MapPolygon,
    useLoadScript
} from "@react-google-maps/api";
import {PolygonDTO} from "@/interfaces/PolygonDTO";
import {RealEstateMapDto} from "@/interfaces/RealEstateMapDto";
import RealEstateMarkerInfo from "@/components/realestate/RealEstateMarkerInfo";
import {RealEstateMarkerInfoMode} from "@/components/realestate/RealEstateMarkerInfoMode";
import {RealEstateFilterParams} from "@/interfaces/RealEstateFilterParams";

/**
 * Props for the polygon map editing panel.
 */
interface PolygonMapPanelProps {
    polygon: PolygonDTO;
    polygonName: string;
    displayedRealEstates: RealEstateMapDto[];
    attachedSet: Set<string>;
    onToggleAttachment: (marker: RealEstateMapDto) => void;

    showNotAttached: boolean;
    setShowNotAttached: (val: boolean) => void;
    showAttached: boolean;
    setShowAttached: (val: boolean) => void;

    onSavePolygon: (newCoords?: { lat: number; lng: number }[]) => void;

    filters: RealEstateFilterParams;
    setFilters: (f: RealEstateFilterParams) => void;

    isUpdating: boolean;
}

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

    const polygonRef = useRef<google.maps.Polygon | null>(null);
    const [selectedMarker, setSelectedMarker] = useState<RealEstateMapDto | null>(null);

    if (!isLoaded) {
        return <div>Loading Google Maps...</div>;
    }

    // In case the polygon has no coordinates
    const path = polygon.coordinates || [];
    let mapCenter = {lat: 40.114955, lng: -111.654923};
    if (path.length > 0) {
        mapCenter = {lat: path[0].lat, lng: path[0].lng};
    }

    // Gather final displayed markers from "displayedRealEstates" directly

    function handleSave() {
        if (!polygonRef.current) {
            onSavePolygon();
            return;
        }
        // Read path from the polygonRef
        const newCoords: { lat: number; lng: number }[] = [];
        const polygonPath = polygonRef.current.getPath();
        for (let i = 0; i < polygonPath.getLength(); i++) {
            const pt = polygonPath.getAt(i);
            newCoords.push({lat: pt.lat(), lng: pt.lng()});
        }
        onSavePolygon(newCoords);
    }


    return (
        <div className="bg-white p-4 rounded shadow">
            <h2 className="text-lg font-semibold mb-2">Polygon Editor: {polygonName}</h2>

            {/* Toggles for attached vs not-attached */}
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

            {/* The Map */}
            <div style={{width: "100%", height: "500px"}} className="border rounded overflow-hidden">
                <GoogleMap
                    mapContainerStyle={{width: "100%", height: "100%"}}
                    center={mapCenter}
                    zoom={10}
                >
                    {path.length > 2 && (
                        <MapPolygon
                            paths={path}
                            editable
                            options={{
                                fillColor: "#00FF00",
                                fillOpacity: 0.3,
                                strokeColor: "#00FF00",
                                strokeWeight: 2,
                            }}
                            onLoad={(poly) => (polygonRef.current = poly)}
                        />
                    )}

                    {displayedRealEstates.map((re) => {
                        if (re.latitude == null || re.longitude == null) return null;
                        const isAttached = attachedSet.has(re.id);

                        return (
                            <AdvancedMarkerElement
                                key={re.id}
                                position={{lat: re.latitude, lng: re.longitude}}
                                icon={{
                                    url: isAttached ? "/yellow-dot.png" : "/red-dot.png",
                                }}
                                onClick={() => setSelectedMarker(re)}
                            />
                        );
                    })}

                    {selectedMarker && selectedMarker.latitude !== null && selectedMarker.longitude !== null && (
                        <InfoWindow
                            position={{
                                lat: selectedMarker.latitude || 0,
                                lng: selectedMarker.longitude || 0,
                            }}
                            // position={{lat: selectedMarker.latitude, lng: selectedMarker.longitude}}
                            onCloseClick={() => setSelectedMarker(null)}
                        >
                            <div>
                                <RealEstateMarkerInfo
                                    realEstate={{
                                        id: selectedMarker.id,
                                        mlsNumber: selectedMarker.mlsNumber || "",
                                        soldTerms: selectedMarker.soldTerms || "",
                                        soldPrice: selectedMarker.soldPrice || "",
                                        taxId: selectedMarker.taxId || "",
                                        address: selectedMarker.address || "",
                                        city: selectedMarker.city || "",
                                        state: selectedMarker.state || "",
                                        zip: selectedMarker.zip || "",
                                        status: selectedMarker.status || "",
                                        listPrice: "",
                                        latitude: String(selectedMarker.latitude),
                                        longitude: String(selectedMarker.longitude),
                                    }}
                                    isAttached={attachedSet.has(selectedMarker.id)}
                                    onToggleAttachment={() => {
                                        onToggleAttachment(selectedMarker);
                                        setSelectedMarker(null);
                                    }}
                                    onClose={() => setSelectedMarker(null)}
                                    mode={RealEstateMarkerInfoMode.POLYGON_EDIT_PAGE}
                                />
                            </div>
                        </InfoWindow>
                    )}
                </GoogleMap>
            </div>

            {/* Save Button */}
            <div className="flex justify-center mt-4 items-center">
                <button
                    onClick={handleSave}
                    disabled={isUpdating}
                    className="px-6 py-3 bg-blue-600 text-white text-lg rounded hover:bg-blue-700"
                >
                    {isUpdating ? "Saving..." : "Save Changes to Polygon"}
                </button>
            </div>
        </div>
    );
}
