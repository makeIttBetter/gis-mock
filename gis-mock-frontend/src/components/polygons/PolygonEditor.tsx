"use client";

import React, {useCallback, useEffect, useRef, useState} from "react";
import {GoogleMap, InfoWindow, Marker, Polygon as MapPolygon, useLoadScript,} from "@react-google-maps/api";

import {fetchPolygonById, updatePolygon} from "@/lib/polygonApi";
import {fetchRealEstateMapData} from "@/lib/realEstateApi";

import {PolygonDTO} from "@/interfaces/PolygonDTO";
import {RealEstateMapDto} from "@/interfaces/RealEstateMapDto";

/**
 * Props for PolygonEditor:
 * - polygonId: the ID of the existing polygon to edit.
 */
interface PolygonEditorProps {
    polygonId: string;
}

/**
 * This component allows you to:
 * 1) Load an existing polygon (name + coords + arcgis IDs).
 * 2) Show **all** Real Estate markers (fetched from the map-data endpoint).
 * 3) Differentiate which markers are "attached" vs "not attached."
 * 4) Click markers to toggle attach/remove from polygon.
 * 5) Edit polygon name.
 * 6) Edit polygon coordinates by dragging shape edges.
 * 7) Save changes via an "Update Polygon" PUT request.
 */
export default function PolygonEditor({polygonId}: PolygonEditorProps) {
    // 1) State: polygon data
    const [polygon, setPolygon] = useState<PolygonDTO | null>(null);
    const [polygonName, setPolygonName] = useState<string>("");

    // ArcGIS links
    const [arcgisLayerUrl, setArcgisLayerUrl] = useState<string | null>(null);
    const [arcgisPolygonUrl, setArcgisPolygonUrl] = useState<string | null>(null);

    // 2) State: real estate markers (for the whole map)
    const [allRealEstates, setAllRealEstates] = useState<RealEstateMapDto[]>([]);

    // 3) Which Real Estate IDs are currently *attached* to this polygon
    //    (We keep them in a Set for easy "add/remove" toggling).
    const [attachedSet, setAttachedSet] = useState<Set<string>>(new Set());

    // 4) Map config
    const {isLoaded} = useLoadScript({
        googleMapsApiKey: process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || "",
        libraries: ["drawing", "geometry"],
    });
    const defaultCenter = {lat: 40.114955, lng: -111.654923};
    const defaultZoom = 10;

    // We'll store the reference to the editable polygon on the map
    const polygonRef = useRef<google.maps.Polygon | null>(null);

    /**
     * Step A: Fetch the polygon by ID, then fill local states:
     * - polygon data
     * - polygon name
     * - set up ArcGIS links
     * - create an "attached" set from polygon.realEstateObjects
     */
    const loadPolygon = useCallback(async () => {
        try {
            const p = await fetchPolygonById(polygonId);
            setPolygon(p);
            setPolygonName(p.name ?? "");

            // ArcGIS links if any
            const arcgisBase =
                "https://indic74dbdb0c967.maps.arcgis.com/home/item.html?id=";
            setArcgisLayerUrl(
                p.arcgisLayerId ? arcgisBase + p.arcgisLayerId : null
            );
            setArcgisPolygonUrl(
                p.arcgisPolygonId ? arcgisBase + p.arcgisPolygonId : null
            );

            // Create a set of attached real estate IDs
            const attachedIds = p.realEstateObjects ?? [];
            setAttachedSet(new Set(attachedIds));
        } catch (error) {
            console.error("Error loading polygon data:", error);
            alert("Failed to load polygon data. See console for details.");
        }
    }, [polygonId]);

    /**
     * Step B: Fetch *all* real estate in minimal (map) form
     * so we can show them on the map (both attached & not attached).
     *
     * If you wanted to filter these in some way, you could pass
     * filters to fetchRealEstateMapData.
     */
    const loadRealEstates = useCallback(async () => {
        try {
            const data = await fetchRealEstateMapData({});
            setAllRealEstates(data);
        } catch (error) {
            console.error("Error loading real estate map data:", error);
            alert("Failed to load real estate map data.");
        }
    }, []);

    // On mount -> load the polygon and all RE
    useEffect(() => {
        loadPolygon();
        loadRealEstates();
    }, [loadPolygon, loadRealEstates]);

    /**
     * Convert polygon's coordinate array into a Google Maps-compatible path.
     */
    function getGooglePath(): google.maps.LatLngLiteral[] {
        if (!polygon?.coordinates) return [];
        return polygon.coordinates.map((c) => ({lat: c.lat, lng: c.lng}));
    }

    /**
     * Read updated coordinates from the polygonRef (the user may have dragged edges).
     */
    function getUpdatedCoordinates(): { lat: number; lng: number }[] {
        if (!polygonRef.current) return polygon?.coordinates ?? [];

        const path = polygonRef.current.getPath();
        const coords: { lat: number; lng: number }[] = [];
        for (let i = 0; i < path.getLength(); i++) {
            const point = path.getAt(i);
            coords.push({lat: point.lat(), lng: point.lng()});
        }
        return coords;
    }

    // InfoWindow handling
    const [selectedMarker, setSelectedMarker] =
        useState<RealEstateMapDto | null>(null);

    // On marker click, open info window
    function handleMarkerClickWithInfo(re: RealEstateMapDto) {
        setSelectedMarker(re);
    }

    // Toggle attach or remove from inside InfoWindow
    function toggleAttachment() {
        if (!selectedMarker) return;
        const {id} = selectedMarker;
        const newSet = new Set(attachedSet);
        if (newSet.has(id)) {
            newSet.delete(id);
        } else {
            newSet.add(id);
        }
        setAttachedSet(newSet);
        setSelectedMarker(null);
    }

    /**
     * Save changes:
     * 1) Get updated polygon name from polygonName state.
     * 2) Get updated coordinates from polygonRef.
     * 3) Convert attachedSet into an array of IDs.
     * 4) Call updatePolygon(polygonId, { name, coordinates, realEstateIds }).
     */
    async function handleSaveChanges() {
        if (!polygon) return;
        try {
            const updatedCoords = getUpdatedCoordinates();
            const realEstateIds = Array.from(attachedSet);

            const updated = await updatePolygon(polygon.id, {
                name: polygonName.trim(),
                coordinates: updatedCoords,
                realEstateIds,
            });

            alert("Polygon updated successfully!");
            // Re-fetch polygon data so we see the latest
            setPolygon(updated);
            setPolygonName(updated.name ?? "");
            // Rebuild the attached set
            setAttachedSet(new Set(updated.realEstateObjects ?? []));
        } catch (err) {
            console.error("Error updating polygon:", err);
            alert("Failed to update polygon. See console for details.");
        }
    }

    if (!isLoaded) {
        return <div>Loading Google Maps...</div>;
    }

    if (!polygon) {
        return <div>Loading polygon details...</div>;
    }

    // If you want to auto-center the map on the polygon, you could compute a bounding box.
    // For simplicity, we just center on the first coordinate (if any).
    let mapCenter = defaultCenter;
    const path = getGooglePath();
    if (path.length > 0) {
        mapCenter = {lat: path[0].lat, lng: path[0].lng};
    }

    return (
        <div className="space-y-4">
            {/* HEADER & FORM */}
            <div className="bg-white p-4 rounded shadow">
                <h2 className="text-xl font-semibold mb-2">Edit Polygon</h2>

                {/* Polygon Name */}
                <label className="block mb-2">
                    <span className="text-sm font-medium">Polygon Name:</span>
                    <input
                        type="text"
                        className="mt-1 block w-full border border-gray-300 rounded p-1"
                        value={polygonName}
                        onChange={(e) => setPolygonName(e.target.value)}
                    />
                </label>

                {/* ArcGIS links */}
                <div className="text-sm text-gray-600 space-y-1">
                    <div>
                        <strong>ArcGIS Data Layer:</strong>{" "}
                        {arcgisLayerUrl ? (
                            <a
                                href={arcgisLayerUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="text-blue-600 underline"
                            >
                                {arcgisLayerUrl}
                            </a>
                        ) : (
                            "Not available"
                        )}
                    </div>
                    <div>
                        <strong>ArcGIS Polygon Layer:</strong>{" "}
                        {arcgisPolygonUrl ? (
                            <a
                                href={arcgisPolygonUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="text-blue-600 underline"
                            >
                                {arcgisPolygonUrl}
                            </a>
                        ) : (
                            "Not available"
                        )}
                    </div>
                </div>
            </div>

            {/* MAP SECTION */}
            <div
                style={{width: "100%", height: "500px"}}
                className="border rounded overflow-hidden"
            >
                <GoogleMap
                    mapContainerStyle={{width: "100%", height: "100%"}}
                    center={mapCenter}
                    zoom={defaultZoom}
                >
                    {/* Editable Polygon in GREEN */}
                    {path.length > 2 && (
                        <MapPolygon
                            paths={path}
                            editable={true}
                            options={{
                                fillColor: "#00FF00",
                                fillOpacity: 0.3,
                                strokeColor: "#00FF00",
                                strokeWeight: 2,
                            }}
                            onLoad={(poly) => (polygonRef.current = poly)}
                        />
                    )}

                    {/* Show Markers for all RE. 
                        - If marker is attached, color YELLOW 
                        - If not attached, color RED 
                        
                       Click marker => opens InfoWindow 
                    */}
                    {allRealEstates.map((re) => {
                        if (re.latitude == null || re.longitude == null) return null;
                        const isAttached = attachedSet.has(re.id);

                        return (
                            <Marker
                                key={re.id}
                                position={{lat: re.latitude, lng: re.longitude}}
                                icon={{
                                    url: isAttached
                                        ? "http://maps.google.com/mapfiles/ms/icons/yellow-dot.png"
                                        : "http://maps.google.com/mapfiles/ms/icons/red-dot.png",
                                }}
                                onClick={() => handleMarkerClickWithInfo(re)}
                            />
                        );
                    })}

                    {/* InfoWindow for selectedMarker */}
                    {selectedMarker && selectedMarker.latitude != null && selectedMarker.longitude != null && (
                        <InfoWindow
                            position={{
                                lat: selectedMarker.latitude,
                                lng: selectedMarker.longitude,
                            }}
                            onCloseClick={() => setSelectedMarker(null)}
                        >
                            <div style={{maxWidth: "200px"}}>
                                <div className="font-semibold text-sm">
                                    Real Estate #{selectedMarker.id}
                                </div>
                                <div className="text-xs">
                                    {selectedMarker.city}, {selectedMarker.state}
                                </div>
                                <div className="mt-2">
                                    <button
                                        onClick={toggleAttachment}
                                        className="px-2 py-1 bg-blue-500 text-white rounded text-xs"
                                    >
                                        {attachedSet.has(selectedMarker.id)
                                            ? "Remove from Polygon"
                                            : "Attach to Polygon"}
                                    </button>
                                </div>
                            </div>
                        </InfoWindow>
                    )}
                </GoogleMap>
            </div>

            {/* SAVE BUTTON */}
            <div className="flex justify-end">
                <button
                    onClick={handleSaveChanges}
                    className="px-4 py-2 bg-blue-600 text-white rounded"
                >
                    Save Changes
                </button>
            </div>
        </div>
    );
}
