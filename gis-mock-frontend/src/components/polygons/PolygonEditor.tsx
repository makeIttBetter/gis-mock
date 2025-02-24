"use client";
import React, {useCallback, useEffect, useRef, useState} from "react";
import {GoogleMap, InfoWindow, Marker, Polygon as MapPolygon, useLoadScript,} from "@react-google-maps/api";
import {fetchPolygonById, updatePolygon} from "@/lib/polygonApi";
import {fetchRealEstateMapData} from "@/lib/realEstateApi";
import {PolygonDTO} from "@/interfaces/PolygonDTO";
import {RealEstateMapDto} from "@/interfaces/RealEstateMapDto";
import {RealEstateFilterParams} from "@/interfaces/RealEstateFilterParams";
import RealEstateFilterForm from "@/components/RealEstateFilterForm";
import RealEstateMarkerInfo from "@/components/realestate/RealEstateMarkerInfo";
import {RealEstateMarkerInfoMode} from "@/components/realestate/RealEstateMarkerInfoMode";

export default function PolygonEditor({polygonId}: { polygonId: string }) {
    // (A) Polygon data
    const [polygon, setPolygon] = useState<PolygonDTO | null>(null);
    const [polygonName, setPolygonName] = useState<string>("");

    // ArcGIS references
    const [arcgisLayerUrl, setArcgisLayerUrl] = useState<string | null>(null);
    const [arcgisPolygonUrl, setArcgisPolygonUrl] = useState<string | null>(null);

    // (B) Real estate markers (combined array)
    const [allRealEstates, setAllRealEstates] = useState<RealEstateMapDto[]>([]);
    const [attachedSet, setAttachedSet] = useState<Set<string>>(new Set());

    // (C) Filter
    const [filters, setFilters] = useState<RealEstateFilterParams>({});

    // (D) Google Maps
    const {isLoaded} = useLoadScript({
        googleMapsApiKey: process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || "",
        libraries: ["drawing", "geometry"],
    });
    const defaultCenter = {lat: 40.114955, lng: -111.654923};
    const defaultZoom = 10;
    const polygonRef = useRef<google.maps.Polygon | null>(null);

    // (E) InfoWindow (selected marker)
    const [selectedMarker, setSelectedMarker] = useState<RealEstateMapDto | null>(null);

    // (F) Toggles
    const [showNotAttached, setShowNotAttached] = useState<boolean>(true);
    const [showAttached, setShowAttached] = useState<boolean>(true);

    // (G) Loading state for "Save Polygon"
    const [isUpdating, setIsUpdating] = useState<boolean>(false);

    /* --------------------------------------
     * 1) Load polygon by ID
     * -------------------------------------- */
    const loadPolygon = useCallback(async () => {
        try {
            const p = await fetchPolygonById(polygonId);
            if (!p) {
                alert("Polygon not found or not accessible.");
                return;
            }
            setPolygon(p);
            setPolygonName(p.name || "");

            const arcgisBase = "https://indic74dbdb0c967.maps.arcgis.com/home/item.html?id=";
            setArcgisLayerUrl(p.arcgisLayerId ? arcgisBase + p.arcgisLayerId : null);
            setArcgisPolygonUrl(p.arcgisPolygonId ? arcgisBase + p.arcgisPolygonId : null);

            // Set attached IDs
            const attachedIds = p.realEstateObjects || [];
            setAttachedSet(new Set(attachedIds));
        } catch (error) {
            console.error("Error loading polygon data:", error);
            alert("Failed to load polygon data.");
        }
    }, [polygonId]);

    /* --------------------------------------
     * 2) Load real estate (filtered + attached)
     *    from fetchRealEstateMapData.
     *    We'll combine them into a single array.
     * -------------------------------------- */
    const loadRealEstates = useCallback(async () => {
        try {
            // fetchRealEstateMapData returns { filtered, attached }
            // We'll combine them into one array, ignoring duplicates if any.
            // The polygonId is not needed if we only want the filter data,
            // but if we want *both*, let's pass the polygonId as well.
            const data = await fetchRealEstateMapData(filters, polygonId);

            const filteredArr = data.filtered ?? [];
            const attachedArr = data.attached ?? [];

            // Combine them, removing duplicates
            // We can do so by ID-based logic:
            const combinedMap = new Map<string, RealEstateMapDto>();
            [...filteredArr, ...attachedArr].forEach((re) => {
                combinedMap.set(re.id, re);
            });
            const combined = Array.from(combinedMap.values());

            setAllRealEstates(combined);
        } catch (error) {
            console.error("Error loading real estate map data:", error);
            alert("Failed to load real estate map data.");
        }
    }, [filters, polygonId]);

    /* --------------------------------------
     * 3) Fetch polygon & real estate on mount
     * -------------------------------------- */
    useEffect(() => {
        loadPolygon();
    }, [loadPolygon]);

    useEffect(() => {
        loadRealEstates();
    }, [loadRealEstates]);

    if (!isLoaded) {
        return <div>Loading Google Maps...</div>;
    }
    if (!polygon) {
        return <div>Loading polygon details...</div>;
    }

    /* --------------------------------------
     * 4) Compute the polygon path
     * -------------------------------------- */
    function getGooglePath(): google.maps.LatLngLiteral[] {
        if (!polygon?.coordinates) return [];
        return polygon.coordinates.map((c) => ({lat: c.lat, lng: c.lng}));
    }

    const path = getGooglePath();

    let mapCenter = defaultCenter;
    if (path.length > 0) {
        mapCenter = {lat: path[0].lat, lng: path[0].lng};
    }

    /* --------------------------------------
     * 5) Decide which real estate to display,
     *    based on 'attachedSet' & toggles
     * -------------------------------------- */
    const displayedRealEstates = allRealEstates.filter((re) => {
        const isAttached = attachedSet.has(re.id);
        if (isAttached && !showAttached) return false;
        return !(!isAttached && !showNotAttached);

    });

    /* --------------------------------------
     * 6) Save changes to polygon in DB
     * -------------------------------------- */
    async function handleSavePolygonChanges() {
        if (!polygon) return;
        setIsUpdating(true);

        try {
            // Rebuild coords from actual polygon if user moved it
            let coords = polygon.coordinates;
            if (polygonRef.current) {
                const pathArr = polygonRef.current.getPath();
                coords = [];
                for (let i = 0; i < pathArr.getLength(); i++) {
                    const pt = pathArr.getAt(i);
                    coords.push({lat: pt.lat(), lng: pt.lng()});
                }
            }

            // Convert attachedSet to array
            const reIds = Array.from(attachedSet);

            // Update polygon
            const updated = await updatePolygon(polygon.id, {
                name: polygonName.trim(),
                coordinates: coords,
                realEstateIds: reIds,
            });

            alert("Polygon updated successfully!");

            setPolygon(updated);
            setPolygonName(updated.name || "");
            setAttachedSet(new Set(updated.realEstateObjects || []));
        } catch (err: any) {
            console.error("Error updating polygon:", err);
            alert("Failed to update polygon.");
        } finally {
            setIsUpdating(false);
        }
    }

    /* --------------------------------------
     * 7) Toggle attach/detach
     * -------------------------------------- */
    function handleToggleAttachment(marker: RealEstateMapDto) {
        const newSet = new Set(attachedSet);
        if (newSet.has(marker.id)) {
            newSet.delete(marker.id);
        } else {
            newSet.add(marker.id);
        }
        setAttachedSet(newSet);
    }

    /* --------------------------------------
     * 8) Return the UI
     * -------------------------------------- */
    return (
        <div className="space-y-4">
            {/* Polygon Info */}
            <div className="bg-white p-4 rounded shadow">
                <h2 className="text-xl font-semibold mb-2">Edit Polygon</h2>
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

            {/* Show/hide toggles */}
            <div className="bg-white p-4 rounded shadow flex gap-8 items-center">
                <label className="flex items-center space-x-2">
                    <input
                        type="checkbox"
                        checked={showNotAttached}
                        onChange={(e) => setShowNotAttached(e.target.checked)}
                    />
                    <span>Show NOT-attached</span>
                </label>
                <label className="flex items-center space-x-2">
                    <input
                        type="checkbox"
                        checked={showAttached}
                        onChange={(e) => setShowAttached(e.target.checked)}
                    />
                    <span>Show attached</span>
                </label>
            </div>

            {/* Map */}
            <div
                style={{width: "100%", height: "500px"}}
                className="border rounded overflow-hidden"
            >
                <GoogleMap
                    mapContainerStyle={{width: "100%", height: "100%"}}
                    center={mapCenter}
                    zoom={defaultZoom}
                >
                    {/* Editable polygon in GREEN */}
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

                    {/* Markers */}
                    {displayedRealEstates.map((re) => {
                        if (
                            re.latitude == null ||
                            re.longitude == null
                        ) {
                            return null;
                        }
                        const isAttached = attachedSet.has(re.id);

                        return (
                            <Marker
                                key={re.id}
                                position={{lat: re.latitude, lng: re.longitude}}
                                icon={{
                                    url: isAttached
                                        ? "/yellow-dot.png"
                                        : "/red-dot.png",
                                }}
                                onClick={() => setSelectedMarker(re)}
                            />
                        );
                    })}

                    {/* Info Window */}
                    {selectedMarker &&
                        selectedMarker.latitude !== null &&
                        selectedMarker.longitude !== null && (
                            <InfoWindow
                                position={{
                                    lat: selectedMarker.latitude,
                                    lng: selectedMarker.longitude,
                                }}
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
                                            latitude: selectedMarker.latitude?.toString() || "",
                                            longitude: selectedMarker.longitude?.toString() || "",
                                        }}
                                        isAttached={attachedSet.has(selectedMarker.id)}
                                        onToggleAttachment={() =>
                                            handleToggleAttachment(selectedMarker)
                                        }
                                        onClose={() => setSelectedMarker(null)}
                                        mode={RealEstateMarkerInfoMode.POLYGON_EDIT_PAGE}
                                    />
                                </div>
                            </InfoWindow>
                        )}
                </GoogleMap>
            </div>

            {/* Save button */}
            <br/>
            <div className="flex justify-center mt-4 items-center">
                <button
                    onClick={handleSavePolygonChanges}
                    className="px-6 py-3 bg-blue-600 text-white text-lg rounded hover:bg-blue-700"
                    disabled={isUpdating}
                >
                    Save Changes to Polygon
                </button>
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
