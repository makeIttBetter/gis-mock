"use client";
import React, { CSSProperties, useCallback, useRef, useState } from "react";
import {
    DrawingManager,
    GoogleMap,
    InfoWindow,
    Marker,
    Polygon as MapPolygon,
    useLoadScript,
} from "@react-google-maps/api";
import { RealEstateMapDto } from "@/interfaces/RealEstateMapDto";
import RealEstateMarkerInfo from "@/components/realestate/RealEstateMarkerInfo";
import { RealEstateMarkerInfoMode } from "@/components/realestate/RealEstateMarkerInfoMode";

const LIBRARIES: ("drawing" | "geometry" | "places" | "visualization")[] = [
    "drawing",
    "geometry",
];

export interface RealEstateMapProps {
    realEstates: RealEstateMapDto[];
    attachedIds?: string[];
    center?: google.maps.LatLngLiteral;
    zoom?: number;
    containerStyle?: CSSProperties;

    displayPolygon?: {
        coordinates: { lat: number; lng: number }[];
    };
    editablePolygon?: {
        coordinates: { lat: number; lng: number }[];
        realEstateObjects: string[];
    };
    onUpdatePolygon?: (updatedPolygon: {
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) => void;
    onCreatePolygon?: (newPolygon: {
        name: string;
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) => void;
}

export default function RealEstateMap({
                                          realEstates,
                                          attachedIds,
                                          center = { lat: 40.114955, lng: -111.654923 },
                                          zoom = 11,
                                          containerStyle = { width: "100%", height: "400px" },
                                          displayPolygon,
                                          editablePolygon,
                                          onUpdatePolygon,
                                          onCreatePolygon,
                                      }: RealEstateMapProps) {
    const { isLoaded } = useLoadScript({
        googleMapsApiKey: process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || "",
        libraries: LIBRARIES,
    });

    const mapRef = useRef<google.maps.Map | null>(null);
    const drawingManagerRef = useRef<google.maps.drawing.DrawingManager | null>(null);

    const [isDrawingActive, setIsDrawingActive] = useState(false);
    const [draftPolygon, setDraftPolygon] = useState<google.maps.Polygon | null>(null);
    const [draftCoords, setDraftCoords] = useState<{ lat: number; lng: number }[] | null>(
        null
    );
    const [editablePolygonInstance, setEditablePolygonInstance] =
        useState<google.maps.Polygon | null>(null);

    // InfoWindow state
    const [selectedRE, setSelectedRE] = useState<RealEstateMapDto | null>(null);

    const onMapLoad = useCallback((map: google.maps.Map) => {
        mapRef.current = map;
    }, []);

    function startDrawing() {
        if (draftPolygon) {
            draftPolygon.setMap(null);
            setDraftPolygon(null);
            setDraftCoords(null);
        }
        setIsDrawingActive(true);
    }

    function cancelDrawing() {
        setIsDrawingActive(false);
        if (drawingManagerRef.current) {
            drawingManagerRef.current.setDrawingMode(null);
        }
    }

    function getPathCoords(path: google.maps.MVCArray<google.maps.LatLng>) {
        const coords: { lat: number; lng: number }[] = [];
        for (let i = 0; i < path.getLength(); i++) {
            const pt = path.getAt(i);
            coords.push({ lat: pt.lat(), lng: pt.lng() });
        }
        return coords;
    }

    const handleOverlayComplete = useCallback(
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
            const coords = getPathCoords(path);
            if (coords.length < 3) {
                polygon.setMap(null);
                return;
            }

            setDraftPolygon(polygon);
            setDraftCoords(coords);

            // Listen for shape changes
            google.maps.event.addListener(path, "set_at", () => {
                setDraftCoords(getPathCoords(path));
            });
            google.maps.event.addListener(path, "insert_at", () => {
                setDraftCoords(getPathCoords(path));
            });
            google.maps.event.addListener(path, "remove_at", () => {
                setDraftCoords(getPathCoords(path));
            });
        },
        [isDrawingActive]
    );

    function handleSaveNewPolygon() {
        if (!draftPolygon || !draftCoords || !onCreatePolygon) return;
        const name = window.prompt("Enter a name for the new polygon:");
        if (!name) return;

        // Determine which real estate objects are inside
        const googlePoly = new google.maps.Polygon({ paths: draftCoords });
        const insideIds: string[] = [];

        realEstates.forEach((re) => {
            if (
                re.latitude !== null &&
                re.latitude !== undefined &&
                re.longitude !== null &&
                re.longitude !== undefined
            ) {
                const pos = new google.maps.LatLng(re.latitude, re.longitude);
                if (google.maps.geometry.poly.containsLocation(pos, googlePoly)) {
                    insideIds.push(re.id);
                }
            }
        });

        onCreatePolygon({
            name,
            coordinates: draftCoords,
            realEstateIds: insideIds,
        });

        draftPolygon.setMap(null);
        setDraftPolygon(null);
        setDraftCoords(null);
    }

    function handleDiscardNewPolygon() {
        if (draftPolygon) {
            draftPolygon.setMap(null);
        }
        setDraftPolygon(null);
        setDraftCoords(null);
    }

    function handleSaveEditablePolygon() {
        if (!editablePolygon || !editablePolygonInstance || !onUpdatePolygon) return;

        const path = editablePolygonInstance.getPath();
        const coords = getPathCoords(path);

        // Determine which real estate objects are inside
        const googlePoly = new google.maps.Polygon({ paths: coords });
        const insideIds: string[] = [];

        realEstates.forEach((re) => {
            if (
                re.latitude !== null &&
                re.latitude !== undefined &&
                re.longitude !== null &&
                re.longitude !== undefined
            ) {
                const pos = new google.maps.LatLng(re.latitude, re.longitude);
                if (google.maps.geometry.poly.containsLocation(pos, googlePoly)) {
                    insideIds.push(re.id);
                }
            }
        });

        if (!window.confirm("Save changes to polygon?")) return;
        onUpdatePolygon({
            coordinates: coords,
            realEstateIds: insideIds,
        });
    }

    if (!isLoaded) {
        return <div>Loading Map...</div>;
    }

    return (
        <div>
            {/* Drawing Controls */}
            {!editablePolygon && (
                <div className="mb-2">
                    {isDrawingActive ? (
                        <button
                            onClick={cancelDrawing}
                            className="px-4 py-2 bg-gray-400 rounded"
                        >
                            Cancel Drawing
                        </button>
                    ) : (
                        <button
                            onClick={startDrawing}
                            className="px-4 py-2 bg-blue-600 text-white rounded"
                        >
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
                    onLoad={(mgr) => (drawingManagerRef.current = mgr)}
                    onOverlayComplete={handleOverlayComplete}
                    drawingMode={
                        isDrawingActive ? google.maps.drawing.OverlayType.POLYGON : null
                    }
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

                {/* Display polygon in read-only (blue) */}
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

                {/* Editable polygon in green */}
                {editablePolygon && (
                    <MapPolygon
                        paths={editablePolygon.coordinates}
                        editable
                        options={{
                            fillColor: "#00FF00",
                            fillOpacity: 0.3,
                            strokeColor: "#00FF00",
                            strokeWeight: 2,
                        }}
                        onLoad={(poly) => setEditablePolygonInstance(poly)}
                    />
                )}

                {/* Markers */}
                {realEstates.map((re) => {
                    if (
                        re.latitude == null ||
                        re.longitude == null ||
                        re.longitude === undefined
                    ) {
                        return null;
                    }
                    const isPolyAttached = attachedIds?.includes(re.id) ?? false;

                    return (
                        <Marker
                            key={re.id}
                            position={{ lat: re.latitude, lng: re.longitude }}
                            icon={{
                                url: isPolyAttached
                                    ? "http://maps.google.com/mapfiles/ms/icons/yellow-dot.png"
                                    : "http://maps.google.com/mapfiles/ms/icons/red-dot.png",
                            }}
                            onClick={() => setSelectedRE(re)}
                        />
                    );
                })}

                {/* InfoWindow w/ editing form */}
                {selectedRE && selectedRE.latitude != null && selectedRE.longitude != null && (
                    <InfoWindow
                        position={{ lat: selectedRE.latitude, lng: selectedRE.longitude }}
                        onCloseClick={() => setSelectedRE(null)}
                    >
                        <div>
                            <RealEstateMarkerInfo
                                realEstate={{
                                    id: selectedRE.id,
                                    mlsNumber: selectedRE.mlsNumber || "",
                                    soldTerms: selectedRE.soldTerms || "",
                                    soldPrice: selectedRE.soldPrice || "",
                                    taxId: selectedRE.taxId || "",
                                    address: selectedRE.address || "",
                                    city: selectedRE.city || "",
                                    state: selectedRE.state || "",
                                    zip: selectedRE.zip || "",
                                    status: selectedRE.status || "",
                                    listPrice: "",
                                    latitude: selectedRE.latitude?.toString() || "",
                                    longitude: selectedRE.longitude?.toString() || "",
                                }}
                                isAttached={attachedIds?.includes(selectedRE.id)}
                                onToggleAttachment={undefined} // not used in the main map
                                onClose={() => setSelectedRE(null)}
                                mode={RealEstateMarkerInfoMode.MAP_PAGE}
                            />
                        </div>
                    </InfoWindow>
                )}
            </GoogleMap>

            {/* Draft polygon controls */}
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

            {/* Editable polygon save button */}
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
