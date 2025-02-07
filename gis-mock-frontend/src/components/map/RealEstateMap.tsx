"use client";
import React, {CSSProperties, useCallback, useEffect, useRef, useState} from "react";
import {
    DrawingManager,
    GoogleMap,
    InfoWindow,
    Marker,
    Polygon as MapPolygon,
    useLoadScript
} from "@react-google-maps/api";
import {RealEstateMapDto} from "@/interfaces/RealEstateMapDto";

const LIBRARIES: ("drawing" | "geometry" | "places" | "visualization")[] = [
    "drawing",
    "geometry"
];

export interface RealEstateMapProps {
    /** All real estate objects for markers (using minimal map DTO). */
    realEstates: RealEstateMapDto[];

    /** Which IDs are considered "attached" (for coloring, etc.). */
    attachedIds?: string[];

    /** Initial center and zoom for the map. */
    center?: google.maps.LatLngLiteral;
    zoom?: number;

    /** CSS for the map container. */
    containerStyle?: CSSProperties;

    /**
     * A polygon to display in read-only mode (blue).
     */
    displayPolygon?: {
        coordinates: { lat: number; lng: number }[];
    };

    /**
     * A polygon to display in "edit mode" (green).
     */
    editablePolygon?: {
        coordinates: { lat: number; lng: number }[];
        realEstateObjects: string[];
    };

    /**
     * Called when user finishes editing an existing polygon
     * and presses the "Save Polygon" button.
     */
    onUpdatePolygon?: (updatedPolygon: {
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) => void;

    /**
     * Called when user finalizes creation of a brand-new polygon
     * (they click "Save New Polygon" after drawing).
     */
    onCreatePolygon?: (newPolygon: {
        name: string;
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) => void;
}

export default function RealEstateMap({
                                          realEstates,
                                          attachedIds,
                                          center = {lat: 40.114955, lng: -111.654923},
                                          zoom = 11,
                                          containerStyle = {width: "100%", height: "400px"},
                                          displayPolygon,
                                          editablePolygon,
                                          onUpdatePolygon,
                                          onCreatePolygon
                                      }: RealEstateMapProps) {
    const {isLoaded} = useLoadScript({
        googleMapsApiKey: process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || "",
        libraries: LIBRARIES
    });

    const mapRef = useRef<google.maps.Map | null>(null);
    const onMapLoad = useCallback((map: google.maps.Map) => {
        mapRef.current = map;
    }, []);

    const [drawingManager, setDrawingManager] =
        useState<google.maps.drawing.DrawingManager | null>(null);

    const [isDrawingActive, setIsDrawingActive] = useState(false);
    const [selectedRE, setSelectedRE] = useState<RealEstateMapDto | null>(null);

    // "Draft" polygon that the user just drew
    const [draftPolygon, setDraftPolygon] = useState<google.maps.Polygon | null>(
        null
    );
    const [draftCoords, setDraftCoords] = useState<
        { lat: number; lng: number }[] | null
    >(null);

    // For editing an existing polygon
    const [editablePolygonInstance, setEditablePolygonInstance] =
        useState<google.maps.Polygon | null>(null);

    // 1. Start or cancel drawing brand-new polygon
    const startDrawing = () => {
        if (draftPolygon) {
            draftPolygon.setMap(null);
            setDraftPolygon(null);
            setDraftCoords(null);
        }
        setIsDrawingActive(true);
    };

    const cancelDrawing = () => {
        if (drawingManager) {
            drawingManager.setDrawingMode(null);
        }
        setIsDrawingActive(false);
    };

    useEffect(() => {
        const handleEsc = (e: KeyboardEvent) => {
            if (e.key === "Escape" && isDrawingActive) {
                cancelDrawing();
            }
        };
        window.addEventListener("keydown", handleEsc);
        return () => window.removeEventListener("keydown", handleEsc);
    }, [isDrawingActive, drawingManager]);

    const onDrawingManagerLoad = useCallback(
        (manager: google.maps.drawing.DrawingManager) => {
            setDrawingManager(manager);
        },
        []
    );

    // 2. When user finishes drawing a polygon
    const onOverlayComplete = useCallback(
        (e: google.maps.drawing.OverlayCompleteEvent) => {
            if (!isDrawingActive) {
                e.overlay.setMap(null);
                return;
            }
            if (e.type !== google.maps.drawing.OverlayType.POLYGON) {
                e.overlay.setMap(null);
                return;
            }
            cancelDrawing();
            const polygon = e.overlay as google.maps.Polygon;
            const path = polygon.getPath();
            const coords: { lat: number; lng: number }[] = [];
            for (let i = 0; i < path.getLength(); i++) {
                const point = path.getAt(i);
                coords.push({lat: point.lat(), lng: point.lng()});
            }
            // must have at least 3 points
            if (coords.length < 3) {
                polygon.setMap(null);
                return;
            }
            setDraftPolygon(polygon);
            setDraftCoords(coords);
        },
        [isDrawingActive, drawingManager]
    );

    // 3. Save or discard the brand-new polygon
    const handleSaveNewPolygon = () => {
        if (!draftPolygon || !draftCoords || !onCreatePolygon) return;
        const name = window.prompt("Enter a name for the new polygon:");
        if (!name) {
            return;
        }
        // Figure out which realEstates are inside the polygon
        const googlePoly = new google.maps.Polygon({paths: draftCoords});
        const insideIds: string[] = [];
        for (const re of realEstates) {
            if (re.latitude !== null && re.longitude !== null) {
                const pos = new google.maps.LatLng(re.latitude, re.longitude);
                if (google.maps.geometry.poly.containsLocation(pos, googlePoly)) {
                    insideIds.push(re.id);
                }
            }
        }
        onCreatePolygon({
            name,
            coordinates: draftCoords,
            realEstateIds: insideIds
        });
        draftPolygon.setMap(null);
        setDraftPolygon(null);
        setDraftCoords(null);
    };

    const handleDiscardNewPolygon = () => {
        if (draftPolygon) {
            draftPolygon.setMap(null);
        }
        setDraftPolygon(null);
        setDraftCoords(null);
    };

    // 4. Edit mode for an existing polygon
    const handleSaveEditablePolygon = () => {
        if (!editablePolygon || !editablePolygonInstance || !onUpdatePolygon) return;
        const path = editablePolygonInstance.getPath();
        const coords: { lat: number; lng: number }[] = [];
        for (let i = 0; i < path.getLength(); i++) {
            const point = path.getAt(i);
            coords.push({lat: point.lat(), lng: point.lng()});
        }
        // figure out which re are inside
        const googlePoly = new google.maps.Polygon({paths: coords});
        const insideIds: string[] = [];
        for (const re of realEstates) {
            if (re.latitude !== null && re.longitude !== null) {
                const pos = new google.maps.LatLng(re.latitude, re.longitude);
                if (google.maps.geometry.poly.containsLocation(pos, googlePoly)) {
                    insideIds.push(re.id);
                }
            }
        }
        const doSave = window.confirm("Save changes to polygon?");
        if (!doSave) return;
        onUpdatePolygon({
            coordinates: coords,
            realEstateIds: insideIds
        });
    };

    if (!isLoaded) {
        return <div>Loading Map...</div>;
    }

    return (
        <div>
            {/* If not editing an existing polygon, show "Draw New Polygon" button */}
            {!editablePolygon && (
                <div className="mb-2">
                    {isDrawingActive ? (
                        <button onClick={cancelDrawing} className="px-4 py-2 bg-gray-400 rounded">
                            Cancel Drawing
                        </button>
                    ) : (
                        <button onClick={startDrawing} className="px-4 py-2 bg-blue-600 text-white rounded">
                            Draw New Polygon
                        </button>
                    )}
                </div>
            )}

            <GoogleMap
                mapContainerStyle={containerStyle}
                center={center}
                zoom={zoom}
                onLoad={onMapLoad}
            >
                <DrawingManager
                    onLoad={onDrawingManagerLoad}
                    onOverlayComplete={onOverlayComplete}
                    drawingMode={isDrawingActive ? google.maps.drawing.OverlayType.POLYGON : null}
                    options={{
                        drawingControl: false,
                        polygonOptions: {
                            fillColor: "#FF0000",
                            fillOpacity: 0.3,
                            strokeColor: "#FF0000",
                            strokeWeight: 2,
                            editable: true,
                            clickable: true
                        }
                    }}
                />

                {/* "draftPolygon" is already rendered by DrawingManager, so no need to re-render it here */}

                {/* read-only polygon in BLUE */}
                {displayPolygon && !editablePolygon && (
                    <MapPolygon
                        paths={displayPolygon.coordinates}
                        options={{
                            fillColor: "#0000FF",
                            fillOpacity: 0.2,
                            strokeColor: "#0000FF",
                            strokeWeight: 2
                        }}
                    />
                )}

                {/* editable polygon in GREEN */}
                {editablePolygon && (
                    <MapPolygon
                        paths={editablePolygon.coordinates}
                        editable
                        options={{
                            fillColor: "#00FF00",
                            fillOpacity: 0.3,
                            strokeColor: "#00FF00",
                            strokeWeight: 2
                        }}
                        onLoad={(poly) => setEditablePolygonInstance(poly)}
                    />
                )}

                {/* Markers */}
                {realEstates.map((re) => {
                    if (re.latitude == null || re.longitude == null) return null;
                    const isAttached = attachedIds?.includes(re.id) ?? false;

                    return (
                        <Marker
                            key={re.id}
                            position={{lat: re.latitude, lng: re.longitude}}
                            icon={{
                                url: isAttached
                                    ? "http://maps.google.com/mapfiles/ms/icons/yellow-dot.png"
                                    : "http://maps.google.com/mapfiles/ms/icons/red-dot.png"
                            }}
                            onClick={() => setSelectedRE(re)}
                        />
                    );
                })}

                {selectedRE && selectedRE.latitude != null && selectedRE.longitude != null && (
                    <InfoWindow
                        position={{lat: selectedRE.latitude, lng: selectedRE.longitude}}
                        onCloseClick={() => setSelectedRE(null)}
                    >
                        <div>
                            <div className="text-xs text-gray-500">ID: {selectedRE.id}</div>
                            <div className="font-semibold">
                                {selectedRE.city}, {selectedRE.state}
                            </div>
                            <div>Status: {selectedRE.status || "N/A"}</div>
                        </div>
                    </InfoWindow>
                )}
            </GoogleMap>

            {/* If user has a "draft" polygon, show Save/Discard */}
            {draftPolygon && draftCoords && (
                <div className="mt-3 flex gap-2">
                    <button
                        onClick={handleSaveNewPolygon}
                        className="px-3 py-1 bg-green-600 text-white rounded"
                    >
                        Save New Polygon
                    </button>
                    <button
                        onClick={handleDiscardNewPolygon}
                        className="px-3 py-1 bg-red-600 text-white rounded"
                    >
                        Discard New Polygon
                    </button>
                </div>
            )}

            {/* If editing existing polygon, show "Save" button */}
            {editablePolygon && onUpdatePolygon && (
                <div className="mt-3">
                    <button
                        onClick={handleSaveEditablePolygon}
                        className="px-3 py-1 bg-green-600 text-white rounded"
                    >
                        Save Edited Polygon
                    </button>
                </div>
            )}
        </div>
    );
}
