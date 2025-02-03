"use client";
import React, { useCallback, useEffect, useRef, useState } from "react";
// Rename the imported Polygon component to MapPolygon to avoid conflicts.
import {
    DrawingManager,
    GoogleMap,
    Marker,
    InfoWindow,
    Polygon as MapPolygon,
    useLoadScript,
} from "@react-google-maps/api";
import { RealEstate } from "@/interfaces/RealEstate";

const LIBRARIES = ["drawing", "geometry"] as (
    | "drawing"
    | "geometry"
    | "places"
    | "visualization"
    )[];

export interface RealEstateMapProps {
    realEstates: RealEstate[];
    center?: google.maps.LatLngLiteral;
    zoom?: number;
    containerStyle?: React.CSSProperties;
    // When provided, displays a polygon in read-only mode.
    displayPolygon?: { coordinates: { lat: number; lng: number }[] };
    // When provided, shows an editable polygon overlay.
    editablePolygon?: { coordinates: { lat: number; lng: number }[]; realEstateObjects: string[] };
    // Callback for updating an existing polygon (after editing)
    onUpdatePolygon?: (updatedPolygon: {
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) => void;
    // Callback for creating a new polygon (from drawing mode)
    onCreatePolygon?: (newPolygon: {
        name: string;
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) => void;
    /**
     * An optional list of real estate IDs that are “attached” to the selected polygon.
     * Markers whose id is in this list will be rendered using a yellow icon.
     */
    attachedIds?: string[];
}

export default function RealEstateMap({
                                          realEstates,
                                          center = { lat: 40.114955, lng: -111.654923 },
                                          zoom = 11,
                                          containerStyle = { width: "100%", height: "400px" },
                                          displayPolygon,
                                          editablePolygon,
                                          onUpdatePolygon,
                                          onCreatePolygon,
                                          attachedIds,
                                      }: RealEstateMapProps) {
    const { isLoaded } = useLoadScript({
        googleMapsApiKey: process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || "",
        libraries: LIBRARIES,
    });
    const mapRef = useRef<google.maps.Map | null>(null);
    const onMapLoad = useCallback((map: google.maps.Map) => {
        mapRef.current = map;
    }, []);

    // --- Force re-rendering ---
    const [mapKey, setMapKey] = useState<number>(0);
    const forceReRenderMap = () => {
        setMapKey((prev) => prev + 1);
    };

    // --- Drawing Mode State ---
    const [isDrawingEnabled, setIsDrawingEnabled] = useState(false);
    const [drawingMode, setDrawingMode] = useState<google.maps.drawing.OverlayType | null>(null);
    const [currentDrawingPolygon, setCurrentDrawingPolygon] = useState<google.maps.Polygon | null>(null);
    const [editablePolygonInstance, setEditablePolygonInstance] = useState<google.maps.Polygon | null>(null);

    // --- State for selected real estate (for InfoWindow) ---
    const [selectedRealEstate, setSelectedRealEstate] = useState<RealEstate | null>(null);

    const startDrawing = () => {
        setIsDrawingEnabled(true);
        setDrawingMode(google.maps.drawing.OverlayType.POLYGON);
    };

    const cancelDrawing = () => {
        if (currentDrawingPolygon) {
            currentDrawingPolygon.setMap(null);
            setCurrentDrawingPolygon(null);
        }
        setDrawingMode(null);
        setIsDrawingEnabled(false);
        forceReRenderMap();
    };

    const undoLastVertex = () => {
        if (currentDrawingPolygon) {
            const path = currentDrawingPolygon.getPath();
            const length = path.getLength();
            if (length > 0) {
                path.removeAt(length - 1);
            }
        }
    };

    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            if (e.key === "Escape" && currentDrawingPolygon) {
                currentDrawingPolygon.setMap(null);
                setCurrentDrawingPolygon(null);
                setDrawingMode(null);
                setIsDrawingEnabled(false);
                forceReRenderMap();
            }
        };
        window.addEventListener("keydown", handleKeyDown);
        return () => window.removeEventListener("keydown", handleKeyDown);
    }, [currentDrawingPolygon]);

    const onPolygonComplete = useCallback(
        (polygon: google.maps.Polygon) => {
            setIsDrawingEnabled(false);
            setDrawingMode(null);
            setCurrentDrawingPolygon(polygon);

            const polygonBounds: { lat: number; lng: number }[] = [];
            const path = polygon.getPath();
            for (let i = 0; i < path.getLength(); i++) {
                const point = path.getAt(i);
                polygonBounds.push({ lat: point.lat(), lng: point.lng() });
            }

            // Compute which real estate markers are inside the drawn polygon.
            const insideIds: string[] = [];
            const googlePolygon = new window.google.maps.Polygon({ paths: polygonBounds });
            realEstates.forEach((re) => {
                const lat = parseFloat(re.latitude);
                const lng = parseFloat(re.longitude);
                if (!isNaN(lat) && !isNaN(lng)) {
                    const position = new google.maps.LatLng(lat, lng);
                    if (window.google.maps.geometry.poly.containsLocation(position, googlePolygon)) {
                        insideIds.push(re.id.toString());
                    }
                }
            });

            const polygonName = window.prompt("Enter a name for the new polygon:");
            if (!polygonName) {
                polygon.setMap(null);
                setCurrentDrawingPolygon(null);
                forceReRenderMap();
                return;
            }

            if (onCreatePolygon) {
                onCreatePolygon({
                    name: polygonName,
                    coordinates: polygonBounds,
                    realEstateIds: insideIds,
                });
            }
            polygon.setMap(null);
            setCurrentDrawingPolygon(null);
            forceReRenderMap();
        },
        [realEstates, onCreatePolygon]
    );

    if (!isLoaded) {
        return <div>Loading Map...</div>;
    }

    return (
        <div>
            {/* Drawing controls */}
            {!editablePolygon && (
                <div className="mb-2">
                    {isDrawingEnabled ? (
                        <>
                            <button onClick={cancelDrawing} className="px-4 py-2 bg-gray-400 rounded">
                                Cancel Drawing
                            </button>
                            <button onClick={undoLastVertex} className="px-4 py-2 bg-orange-500 rounded ml-2">
                                Undo Last Vertex
                            </button>
                        </>
                    ) : (
                        <button onClick={startDrawing} className="px-4 py-2 bg-blue-500 text-white rounded">
                            Draw Polygon
                        </button>
                    )}
                </div>
            )}

            <GoogleMap key={mapKey} mapContainerStyle={containerStyle} center={center} zoom={zoom} onLoad={onMapLoad}>
                {drawingMode && !editablePolygon && (
                    <DrawingManager
                        key={`drawing-${drawingMode}`}
                        onPolygonComplete={onPolygonComplete}
                        drawingMode={drawingMode}
                        options={{
                            drawingControl: false,
                            polygonOptions: {
                                fillColor: "#FF0000",
                                fillOpacity: 0.3,
                                strokeColor: "#FF0000",
                                strokeWeight: 2,
                                editable: true,
                                clickable: true,
                            },
                        }}
                    />
                )}

                {editablePolygon && (
                    <MapPolygon
                        paths={editablePolygon.coordinates}
                        editable={true}
                        options={{
                            fillColor: "#00FF00",
                            fillOpacity: 0.3,
                            strokeColor: "#00FF00",
                            strokeWeight: 2,
                        }}
                        onLoad={(polygon) => setEditablePolygonInstance(polygon)}
                    />
                )}

                {displayPolygon && !editablePolygon && (
                    <MapPolygon
                        paths={displayPolygon.coordinates}
                        options={{
                            fillColor: "#0000FF",
                            fillOpacity: 0.2,
                            strokeColor: "#0000FF",
                            strokeWeight: 2,
                        }}
                    />
                )}

                {/* Render markers with custom icon colors and click handling */}
                {realEstates.map((re) => {
                    const lat = parseFloat(re.latitude);
                    const lng = parseFloat(re.longitude);
                    if (isNaN(lat) || isNaN(lng)) return null;
                    const isAttached = attachedIds ? attachedIds.includes(re.id.toString()) : false;
                    const markerIconUrl = isAttached
                        ? "http://maps.google.com/mapfiles/ms/icons/yellow-dot.png"
                        : "http://maps.google.com/mapfiles/ms/icons/red-dot.png";
                    return (
                        <Marker
                            key={re.id}
                            position={{ lat, lng }}
                            icon={{ url: markerIconUrl }}
                            onClick={() => setSelectedRealEstate(re)}
                        />
                    );
                })}

                {/* InfoWindow for selected real estate */}
                {selectedRealEstate && (
                    <InfoWindow
                        position={{
                            lat: parseFloat(selectedRealEstate.latitude),
                            lng: parseFloat(selectedRealEstate.longitude),
                        }}
                        onCloseClick={() => setSelectedRealEstate(null)}
                    >
                        <div style={{ fontSize: "0.9rem" }}>
                            <div className="text-xs text-gray-500">ID: {selectedRealEstate.id}</div>
                            <div className="font-semibold">{selectedRealEstate.address}</div>
                            <div>
                                {selectedRealEstate.city}, {selectedRealEstate.state} {selectedRealEstate.zip}
                            </div>
                            <div>
                                Status: {selectedRealEstate.status} | Price:{" "}
                                {selectedRealEstate.listPrice && selectedRealEstate.listPrice.trim() !== ""
                                    ? selectedRealEstate.listPrice
                                    : "N/A"}
                            </div>
                        </div>
                    </InfoWindow>
                )}
            </GoogleMap>

            {/* When editing, show the "Save Polygon" button */}
            {editablePolygon && onUpdatePolygon && editablePolygonInstance && (
                <div className="mt-2">
                    <button
                        onClick={() => {
                            if (window.confirm("Save changes to polygon?")) {
                                const path = editablePolygonInstance.getPath();
                                const newCoordinates: { lat: number; lng: number }[] = [];
                                for (let i = 0; i < path.getLength(); i++) {
                                    const point = path.getAt(i);
                                    newCoordinates.push({ lat: point.lat(), lng: point.lng() });
                                }
                                const updatedInsideIds: string[] = [];
                                const googlePolygon = new window.google.maps.Polygon({ paths: newCoordinates });
                                realEstates.forEach((re) => {
                                    const lat = parseFloat(re.latitude);
                                    const lng = parseFloat(re.longitude);
                                    if (!isNaN(lat) && !isNaN(lng)) {
                                        const position = new google.maps.LatLng(lat, lng);
                                        if (window.google.maps.geometry.poly.containsLocation(position, googlePolygon)) {
                                            updatedInsideIds.push(re.id.toString());
                                        }
                                    }
                                });
                                onUpdatePolygon({ coordinates: newCoordinates, realEstateIds: updatedInsideIds });
                                forceReRenderMap();
                            }
                        }}
                        className="px-4 py-2 bg-green-600 text-white rounded"
                    >
                        Save Polygon
                    </button>
                </div>
            )}
        </div>
    );
}
