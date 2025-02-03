// File: frontend/src/components/map/RealEstateMap.tsx
"use client";

import React, { useState, useCallback, useRef } from "react";
import {
    GoogleMap,
    useLoadScript,
    Marker,
    DrawingManager,
    InfoWindow,
} from "@react-google-maps/api";
import { RealEstate } from "@/interfaces/RealEstate";
import { createPolygon } from "@/lib/polygonApi";
import { markBaseObject } from "@/lib/realEstateApi";

const LIBRARIES = ["drawing", "geometry"] as (
    | "drawing"
    | "geometry"
    | "places"
    | "visualization"
    )[];

interface RealEstateMapProps {
    realEstates: RealEstate[];
    center?: google.maps.LatLngLiteral;
    zoom?: number;
    containerStyle?: React.CSSProperties;
}

export function RealEstateMap({
                                  realEstates,
                                  center = { lat: 40.114955, lng: -111.654923 },
                                  zoom = 11,
                                  containerStyle = { width: "100%", height: "400px" },
                              }: RealEstateMapProps) {
    const [drawingMode, setDrawingMode] =
        useState<google.maps.drawing.OverlayType | null>(null);
    const [selectedRE, setSelectedRE] = useState<RealEstate | null>(null);

    const { isLoaded } = useLoadScript({
        googleMapsApiKey: process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || "",
        libraries: LIBRARIES,
    });

    const mapRef = useRef<google.maps.Map | null>(null);
    const onMapLoad = useCallback((map: google.maps.Map) => {
        mapRef.current = map;
    }, []);

    function handleStartPolygon() {
        setDrawingMode(google.maps.drawing.OverlayType.POLYGON);
    }
    function handleCancelDrawing() {
        setDrawingMode(null);
    }

    const onPolygonComplete = useCallback(
        async (polygon: google.maps.Polygon) => {
            // Turn off drawing mode
            setDrawingMode(null);

            // Get polygon path (list of lat/lng points)
            const polygonBounds: Array<{ lat: number; lng: number }> = [];
            const path = polygon.getPath();
            for (let i = 0; i < path.getLength(); i++) {
                const point = path.getAt(i);
                polygonBounds.push({ lat: point.lat(), lng: point.lng() });
            }

            // Determine which real estate objects are inside the polygon
            const insideIds: number[] = [];
            const googlePolygon = new window.google.maps.Polygon({
                paths: polygonBounds,
            });
            realEstates.forEach((re) => {
                const latNum = parseFloat(re.latitude);
                const lngNum = parseFloat(re.longitude);
                if (!isNaN(latNum) && !isNaN(lngNum)) {
                    const position = new google.maps.LatLng(latNum, lngNum);
                    const isInside = window.google.maps.geometry.poly.containsLocation(
                        position,
                        googlePolygon
                    );
                    if (isInside) {
                        insideIds.push(re.id);
                    }
                }
            });

            // Ask the user for a polygon name
            const polygonName = window.prompt(
                "Enter a name for this polygon (e.g. 'Spanish Fork East Polygon'):"
            );
            if (!polygonName) {
                return;
            }

            // Save the polygon to the backend
            try {
                await createPolygon(polygonName, polygonBounds, insideIds);
                alert("Polygon saved successfully!");
                // Dispatch a custom event so that the polygons list refreshes automatically
                window.dispatchEvent(new Event("polygonCreated"));
            } catch (error) {
                console.error("Error saving polygon:", error);
                alert("Failed to save polygon");
            }

            // Remove the drawn polygon from the map
            polygon.setMap(null);
        },
        [realEstates]
    );

    function handleMarkerClick(re: RealEstate) {
        setSelectedRE(re);
    }

    async function handleMarkBase(re: RealEstate) {
        try {
            await markBaseObject(re.id);
            alert(`Marked MLS#${re.mlsNumber} as BASE object!`);
            setSelectedRE(null);
        } catch (err) {
            console.error("Error marking base object:", err);
            alert("Failed to mark base object");
        }
    }

    if (!isLoaded) {
        return <div>Loading Map...</div>;
    }

    return (
        <div>
            <div className="mb-2">
                {drawingMode ? (
                    <button
                        onClick={handleCancelDrawing}
                        className="px-4 py-2 bg-gray-400 rounded"
                    >
                        Cancel Drawing
                    </button>
                ) : (
                    <button
                        onClick={handleStartPolygon}
                        className="px-4 py-2 bg-blue-500 text-white rounded"
                    >
                        Draw Polygon
                    </button>
                )}
            </div>

            <GoogleMap
                mapContainerStyle={containerStyle}
                center={center}
                zoom={zoom}
                onLoad={onMapLoad}
            >
                {drawingMode && (
                    <DrawingManager
                        onPolygonComplete={onPolygonComplete}
                        options={{
                            drawingControl: false,
                            polygonOptions: {
                                fillColor: "#FF0000",
                                fillOpacity: 0.3,
                                strokeColor: "#FF0000",
                                strokeWeight: 2,
                                clickable: true,
                                editable: false,
                                zIndex: 1,
                            },
                        }}
                        drawingMode={drawingMode}
                    />
                )}

                {realEstates.map((re) => {
                    const lat = parseFloat(re.latitude);
                    const lng = parseFloat(re.longitude);
                    if (!isNaN(lat) && !isNaN(lng)) {
                        return (
                            <Marker
                                key={re.id}
                                position={{ lat, lng }}
                                onClick={() => handleMarkerClick(re)}
                            />
                        );
                    }
                    return null;
                })}

                {selectedRE && (
                    <InfoWindow
                        position={{
                            lat: parseFloat(selectedRE.latitude),
                            lng: parseFloat(selectedRE.longitude),
                        }}
                        onCloseClick={() => setSelectedRE(null)}
                    >
                        <div style={{ maxWidth: "200px" }}>
                            <h2 className="font-bold mb-1">MLS# {selectedRE.mlsNumber}</h2>
                            <p>{selectedRE.address}</p>
                            <p>
                                {selectedRE.city}, {selectedRE.state} {selectedRE.zip}
                            </p>
                            <button
                                className="mt-2 px-4 py-2 bg-green-500 text-white rounded"
                                onClick={() => handleMarkBase(selectedRE)}
                            >
                                Mark as BASE
                            </button>
                        </div>
                    </InfoWindow>
                )}
            </GoogleMap>
        </div>
    );
}
