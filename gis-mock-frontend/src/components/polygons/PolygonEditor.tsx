"use client";

import React, {useCallback, useEffect, useRef, useState,} from "react";
import {GoogleMap, InfoWindow, Marker, Polygon as MapPolygon, useLoadScript,} from "@react-google-maps/api";

import {fetchPolygonById, updatePolygon} from "@/lib/polygonApi";
import {fetchRealEstateMapData} from "@/lib/realEstateApi";

import {PolygonDTO} from "@/interfaces/PolygonDTO";
import {RealEstateMapDto} from "@/interfaces/RealEstateMapDto";
import {RealEstateFilterParams} from "@/interfaces/RealEstateFilterParams";

// Import the existing filter form
import RealEstateFilterForm from "@/components/RealEstateFilterForm";

interface PolygonEditorProps {
    /** The ID of the existing polygon to edit. */
    polygonId: string;
}

/**
 * This component allows you to:
 * 1) Load an existing polygon (name + coords + arcgis IDs).
 * 2) Show **all** Real Estate markers (or a filtered subset).
 * 3) Differentiate which markers are "attached" vs "not attached."
 * 4) Click markers to toggle attach/remove from polygon.
 * 5) Edit polygon name.
 * 6) Edit polygon coordinates by dragging shape edges.
 * 7) Save changes via an "Update Polygon" PUT request.
 * 8) Use a filter form (like in the RealEstateDashboard) to limit markers shown.
 * 9) Show/hide *attached* (yellow) vs *not-attached* (red) markers via two checkboxes.
 * 10) Show a small loading GIF next to the "Save Changes" button while the update is in progress.
 */
export default function PolygonEditor({polygonId}: PolygonEditorProps) {
    // (A) Polygon data
    const [polygon, setPolygon] = useState<PolygonDTO | null>(null);
    const [polygonName, setPolygonName] = useState<string>("");

    // (B) ArcGIS links
    const [arcgisLayerUrl, setArcgisLayerUrl] = useState<string | null>(null);
    const [arcgisPolygonUrl, setArcgisPolygonUrl] = useState<string | null>(null);

    // (C) Real estate markers
    const [allRealEstates, setAllRealEstates] = useState<RealEstateMapDto[]>([]);
    const [attachedSet, setAttachedSet] = useState<Set<string>>(new Set());

    // (D) Filter state
    const [filters, setFilters] = useState<RealEstateFilterParams>({
        city: "",
        state: "",
        status: "",
        minPrice: "",
        maxPrice: "",
        ids: "",
        address: "",
        zipcode: "",
        propertyTypes: [],
        styles: [],
        yearBuiltMin: undefined,
        yearBuiltMax: undefined,
        glaMin: undefined,
        glaMax: undefined,
        basementSqFtMin: undefined,
        basementSqFtMax: undefined,
        basementFinished: undefined,
        daysBackMin: undefined,
        daysBackMax: undefined,
    });

    // (E) Google Maps setup
    const {isLoaded} = useLoadScript({
        googleMapsApiKey: process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || "",
        libraries: ["drawing", "geometry"],
    });
    const defaultCenter = {lat: 40.114955, lng: -111.654923};
    const defaultZoom = 10;
    const polygonRef = useRef<google.maps.Polygon | null>(null);

    // (F) InfoWindow handling
    const [selectedMarker, setSelectedMarker] = useState<RealEstateMapDto | null>(
        null
    );

    // (G) Checkboxes to show/hide attached vs. not-attached
    const [showNotAttached, setShowNotAttached] = useState<boolean>(true);
    const [showAttached, setShowAttached] = useState<boolean>(true);

    // (H) Loading state for "Save Changes" button
    const [isUpdating, setIsUpdating] = useState<boolean>(false);

    //--------------------------------------------------------------------
    // 1. Load polygon data by ID
    //--------------------------------------------------------------------
    const loadPolygon = useCallback(async () => {
        try {
            const p = await fetchPolygonById(polygonId);
            setPolygon(p);
            setPolygonName(p.name ?? "");

            // ArcGIS links
            const arcgisBase =
                "https://indic74dbdb0c967.maps.arcgis.com/home/item.html?id=";
            setArcgisLayerUrl(p.arcgisLayerId ? arcgisBase + p.arcgisLayerId : null);
            setArcgisPolygonUrl(
                p.arcgisPolygonId ? arcgisBase + p.arcgisPolygonId : null
            );

            // Set of attached IDs
            const attachedIds = p.realEstateObjects ?? [];
            setAttachedSet(new Set(attachedIds));
        } catch (error) {
            console.error("Error loading polygon data:", error);
            alert("Failed to load polygon data. See console for details.");
        }
    }, [polygonId]);

    //--------------------------------------------------------------------
    // 2. Fetch Real Estate map data based on filters
    //--------------------------------------------------------------------
    const loadRealEstates = useCallback(async () => {
        try {
            const data = await fetchRealEstateMapData(filters);
            setAllRealEstates(data);
        } catch (error) {
            console.error("Error loading real estate map data:", error);
            alert("Failed to load real estate map data.");
        }
    }, [filters]);

    // On mount -> load polygon
    useEffect(() => {
        loadPolygon();
    }, [loadPolygon]);

    // On filters change -> load real estate
    useEffect(() => {
        loadRealEstates();
    }, [loadRealEstates]);

    //--------------------------------------------------------------------
    // 3. Helpers
    //--------------------------------------------------------------------
    /** Convert polygon's coordinate array into a Google Maps-compatible path. */
    function getGooglePath(): google.maps.LatLngLiteral[] {
        if (!polygon?.coordinates) return [];
        return polygon.coordinates.map((c) => ({lat: c.lat, lng: c.lng}));
    }

    /** Read updated coordinates from polygonRef after user drags edges. */
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

    /** On marker click, open InfoWindow. */
    function handleMarkerClickWithInfo(re: RealEstateMapDto) {
        setSelectedMarker(re);
    }

    /** Toggle attach or remove from polygon. */
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

    //--------------------------------------------------------------------
    // 4. Save Changes (name, coords, attached set)
    //--------------------------------------------------------------------
    async function handleSaveChanges() {
        if (!polygon) return;
        setIsUpdating(true); // start loading spinner
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
            setAttachedSet(new Set(updated.realEstateObjects ?? []));
        } catch (err) {
            console.error("Error updating polygon:", err);
            alert("Failed to update polygon. See console for details.");
        } finally {
            setIsUpdating(false); // stop loading spinner
        }
    }

    //--------------------------------------------------------------------
    // 5. Render
    //--------------------------------------------------------------------
    if (!isLoaded) {
        return <div>Loading Google Maps...</div>;
    }
    if (!polygon) {
        return <div>Loading polygon details...</div>;
    }

    // Center the map on the first coordinate if available
    const path = getGooglePath();
    let mapCenter = defaultCenter;
    if (path.length > 0) {
        mapCenter = {lat: path[0].lat, lng: path[0].lng};
    }

    // Decide which real estate markers to display, based on checkboxes
    const displayedRealEstates = allRealEstates.filter((re) => {
        const isAttached = attachedSet.has(re.id);
        if (isAttached && !showAttached) return false;     // Attached but hidden
        if (!isAttached && !showNotAttached) return false; // Not attached but hidden
        return true;
    });

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

            {/* Filter Form */}
            <div className="bg-white p-4 rounded shadow">
                <h3 className="text-lg font-semibold mb-2">Filter Real Estate</h3>
                <RealEstateFilterForm
                    filters={filters}
                    onChange={(newFilters) => setFilters(newFilters)}
                />
            </div>

            {/* Checkboxes for show/hide attached and not-attached */}
            <div className="bg-white p-4 rounded shadow flex gap-8 items-center">
                <label className="flex items-center space-x-2">
                    <input
                        type="checkbox"
                        checked={showNotAttached}
                        onChange={(e) => setShowNotAttached(e.target.checked)}
                    />
                    <span>Show NOT-attached to Polygon</span>
                </label>
                <label className="flex items-center space-x-2">
                    <input
                        type="checkbox"
                        checked={showAttached}
                        onChange={(e) => setShowAttached(e.target.checked)}
                    />
                    <span>Show attached to Polygon</span>
                </label>
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

                    {/* Markers for displayed real estate */}
                    {displayedRealEstates.map((re) => {
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
                    {selectedMarker &&
                        selectedMarker.latitude != null &&
                        selectedMarker.longitude != null && (
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

            <br/>
            {/* SAVE BUTTON + Loading GIF */}
            <div className="flex justify-center mt-4 items-center">
                <button
                    onClick={handleSaveChanges}
                    className="
            px-6
            py-3
            bg-blue-600
            text-white
            text-lg
            rounded
            hover:bg-blue-700
            active:bg-blue-800
            transition-colors
            duration-200
          "
                    disabled={isUpdating} // Optionally disable the button while updating
                >
                    Save Changes
                </button>

                {/* Show a small spinner GIF while polygon updates */}
                {isUpdating && (
                    <img
                        src="https://media.tenor.com/On7kvXhzml4AAAAj/loading-gif.gif"
                        alt="Loading..."
                        className="w-6 h-6 ml-3"
                    />
                )}
            </div>
            <br/>
            <br/>
        </div>
    );
}
