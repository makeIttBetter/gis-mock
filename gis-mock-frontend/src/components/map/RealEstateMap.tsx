"use client";
import React, {
    useCallback,
    useEffect,
    useRef,
    useState,
    CSSProperties
} from "react";
import {
    GoogleMap,
    Marker,
    InfoWindow,
    Polygon as MapPolygon,
    DrawingManager,
    useLoadScript
} from "@react-google-maps/api";
import { RealEstate } from "@/interfaces/RealEstate";

const LIBRARIES: (
    | "drawing"
    | "geometry"
    | "places"
    | "visualization"
    )[] = ["drawing", "geometry"];

/**
 * The props for our RealEstateMap component.
 */
export interface RealEstateMapProps {
    /** All real estate objects for markers. */
    realEstates: RealEstate[];

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
     * (i.e. they click "Save New Polygon", enter a name, etc.).
     */
    onCreatePolygon?: (newPolygon: {
        name: string;
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) => void;

    /**
     * Real Estate IDs "attached" to the selected polygon, so we color them differently.
     */
    attachedIds?: string[];
}

/**
 * RealEstateMap: handles markers, the existing polygon,
 * and a "draw new polygon" feature that only prompts for a name when saved.
 */
export default function RealEstateMap({
                                          realEstates,
                                          center = { lat: 40.114955, lng: -111.654923 },
                                          zoom = 11,
                                          containerStyle = { width: "100%", height: "400px" },
                                          displayPolygon,
                                          editablePolygon,
                                          onUpdatePolygon,
                                          onCreatePolygon,
                                          attachedIds
                                      }: RealEstateMapProps) {
    const { isLoaded } = useLoadScript({
        googleMapsApiKey: process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || "",
        libraries: LIBRARIES
    });

    // A ref to the Google Map instance.
    const mapRef = useRef<google.maps.Map | null>(null);
    const onMapLoad = useCallback((map: google.maps.Map) => {
        mapRef.current = map;
    }, []);

    // Reference to the DrawingManager instance.
    const [drawingManager, setDrawingManager] = useState<google.maps.drawing.DrawingManager | null>(null);

    // Track if the user is actively drawing a polygon right now.
    const [isDrawingActive, setIsDrawingActive] = useState(false);

    // Selected real estate for InfoWindow.
    const [selectedRE, setSelectedRE] = useState<RealEstate | null>(null);

    // === NEW: Store a "draft" polygon that the user just drew, which they can save or discard. ===
    // The Google Maps Polygon instance:
    const [draftPolygon, setDraftPolygon] = useState<google.maps.Polygon | null>(null);
    // The list of coordinates for the draft polygon:
    const [draftCoords, setDraftCoords] = useState<{ lat: number; lng: number }[] | null>(null);

    // === Handling "edit mode" polygon instance ===
    const [editablePolygonInstance, setEditablePolygonInstance] =
        useState<google.maps.Polygon | null>(null);

    // -------------------------------------------
    // 1. Start or cancel drawing brand-new polygon
    // -------------------------------------------
    const startDrawing = () => {
        // If there's an existing draft polygon not yet saved, discard it first.
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

    // Pressing ESC should cancel drawing if it’s active.
    useEffect(() => {
        const handleEsc = (e: KeyboardEvent) => {
            if (e.key === "Escape" && isDrawingActive) {
                cancelDrawing();
            }
        };
        window.addEventListener("keydown", handleEsc);
        return () => window.removeEventListener("keydown", handleEsc);
    }, [isDrawingActive, drawingManager]);

    // Called when the drawing manager is ready.
    const onDrawingManagerLoad = useCallback(
        (manager: google.maps.drawing.DrawingManager) => {
            setDrawingManager(manager);
        },
        []
    );

    // -----------------------------------------------
    // 2. onOverlayComplete - user finished a new shape
    // -----------------------------------------------
    const onOverlayComplete = useCallback(
        (e: google.maps.drawing.OverlayCompleteEvent) => {
            if (!isDrawingActive) {
                // If for some reason the user wasn't truly drawing, remove.
                e.overlay.setMap(null);
                return;
            }

            // We only care about polygons.
            if (e.type !== google.maps.drawing.OverlayType.POLYGON) {
                e.overlay.setMap(null);
                return;
            }

            // The polygon was completed: turn off drawing mode
            cancelDrawing();

            const polygon = e.overlay as google.maps.Polygon;
            const path = polygon.getPath();
            const coords: { lat: number; lng: number }[] = [];

            for (let i = 0; i < path.getLength(); i++) {
                const point = path.getAt(i);
                coords.push({ lat: point.lat(), lng: point.lng() });
            }

            // If polygon has fewer than 3 points, discard it
            if (coords.length < 3) {
                polygon.setMap(null);
                return;
            }

            // Store as a "draft" polygon in state:
            setDraftPolygon(polygon);
            setDraftCoords(coords);
        },
        [isDrawingActive, drawingManager]
    );

    // -------------------------------------------------------------------
    // 3. If there's a draft polygon, the user can "Save" or "Discard" it.
    //    We only prompt for the polygon name on "Save".
    // -------------------------------------------------------------------
    const handleSaveNewPolygon = () => {
        if (!draftPolygon || !draftCoords || !onCreatePolygon) return;

        const name = window.prompt("Enter a name for the new polygon:");
        if (!name) {
            // If user canceled the prompt or left it empty, do nothing
            return;
        }

        // Figure out which real estates are inside this polygon
        const googlePoly = new google.maps.Polygon({ paths: draftCoords });
        const insideIds: string[] = [];
        for (const re of realEstates) {
            const lat = parseFloat(re.latitude);
            const lng = parseFloat(re.longitude);
            if (!isNaN(lat) && !isNaN(lng)) {
                const pos = new google.maps.LatLng(lat, lng);
                if (google.maps.geometry.poly.containsLocation(pos, googlePoly)) {
                    insideIds.push(re.id.toString());
                }
            }
        }

        // Finally create it via callback
        onCreatePolygon({
            name,
            coordinates: draftCoords,
            realEstateIds: insideIds
        });

        // Remove from map and reset draft
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

    // -----------------------------------------------------------
    // 4. Handling "edit mode" for an existing polygon (green one)
    // -----------------------------------------------------------
    const handleSaveEditablePolygon = () => {
        if (!editablePolygon || !editablePolygonInstance || !onUpdatePolygon) return;

        // Collect the updated coords
        const path = editablePolygonInstance.getPath();
        const coords: { lat: number; lng: number }[] = [];
        for (let i = 0; i < path.getLength(); i++) {
            const point = path.getAt(i);
            coords.push({ lat: point.lat(), lng: point.lng() });
        }

        // Determine which RealEstate is inside these coords
        const googlePoly = new google.maps.Polygon({ paths: coords });
        const insideIds: string[] = [];
        for (const re of realEstates) {
            const lat = parseFloat(re.latitude);
            const lng = parseFloat(re.longitude);
            if (!isNaN(lat) && !isNaN(lng)) {
                const pos = new google.maps.LatLng(lat, lng);
                if (google.maps.geometry.poly.containsLocation(pos, googlePoly)) {
                    insideIds.push(re.id.toString());
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

    // If the map isn't loaded, show a fallback
    if (!isLoaded) {
        return <div>Loading Map...</div>;
    }

    return (
        <div>
            {/* Buttons for starting/canceling a new polygon (only if not in "edit mode") */}
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
                    // If isDrawingActive, let user draw polygons; otherwise none
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
                            clickable: true
                        }
                    }}
                />

                {/* If we have a 'draft' polygon, show it as-is. It's already on the map. */}
                {/* The user will see it because we haven't removed it from the map. */}
                {/* We'll provide Save/Discard buttons below to finalize or remove it. */}

                {/* Show a "static" polygon in BLUE if displayPolygon is given and we're not editing. */}
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

                {/* Show an "editable" polygon in GREEN if provided. */}
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

                {/* Markers for each RealEstate object */}
                {realEstates.map((re) => {
                    const lat = parseFloat(re.latitude);
                    const lng = parseFloat(re.longitude);
                    if (isNaN(lat) || isNaN(lng)) return null;

                    // Color attached ones differently
                    const isAttached = attachedIds?.includes(String(re.id));

                    return (
                        <Marker
                            key={re.id}
                            position={{ lat, lng }}
                            icon={{
                                url: isAttached
                                    ? "http://maps.google.com/mapfiles/ms/icons/yellow-dot.png"
                                    : "http://maps.google.com/mapfiles/ms/icons/red-dot.png"
                            }}
                            onClick={() => setSelectedRE(re)}
                        />
                    );
                })}

                {/* If the user clicked a marker, show an InfoWindow */}
                {selectedRE && (
                    <InfoWindow
                        position={{
                            lat: parseFloat(selectedRE.latitude),
                            lng: parseFloat(selectedRE.longitude)
                        }}
                        onCloseClick={() => setSelectedRE(null)}
                    >
                        <div>
                            <div className="text-xs text-gray-500">ID: {selectedRE.id}</div>
                            <div className="font-semibold">{selectedRE.address}</div>
                            <div>
                                {selectedRE.city}, {selectedRE.state} {selectedRE.zip}
                            </div>
                            <div>
                                Status: {selectedRE.status} | Price: {selectedRE.listPrice || "N/A"}
                            </div>
                        </div>
                    </InfoWindow>
                )}
            </GoogleMap>

            {/* If user has a "draft" polygon, give them buttons to Save or Discard */}
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

            {/* If we are editing an existing polygon, show a "Save" button */}
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
