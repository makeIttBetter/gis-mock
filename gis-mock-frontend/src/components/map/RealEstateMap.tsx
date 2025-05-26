// File: src/components/map/RealEstateMap.tsx
/* eslint-disable react-hooks/exhaustive-deps */
"use client";

import React, {CSSProperties, useCallback, useEffect, useRef, useState,} from "react";
import {DrawingManager, GoogleMap, InfoWindow, Polygon as MapPolygon, useLoadScript,} from "@react-google-maps/api";
import debounce from "lodash.debounce";
import {RealEstateMapDto} from "@/interfaces/RealEstateMapDto";
import RealEstateMarkerInfo from "@/components/realestate/RealEstateMarkerInfo";
import {RealEstateMarkerInfoMode} from "@/components/realestate/RealEstateMarkerInfoMode";

/* ------------------------------------------------------------------ */
/* Constants                                                          */
/* ------------------------------------------------------------------ */

const LIBRARIES: ("drawing" | "geometry")[] = ["drawing", "geometry"];
/** Persists last camera position among component un-mounts            */
let globalCenter: google.maps.LatLngLiteral | null = null;

/* ------------------------------------------------------------------ */
/* Types                                                              */

/* ------------------------------------------------------------------ */

export interface RealEstateMapProps {
    realEstates: RealEstateMapDto[];
    attachedIds?: string[];
    center?: google.maps.LatLngLiteral;
    zoom?: number;
    containerStyle?: CSSProperties;

    displayPolygon?: { coordinates: { lat: number; lng: number }[] };
    editablePolygon?: {
        coordinates: { lat: number; lng: number }[];
        realEstateObjects: string[];
    };

    onUpdatePolygon?: (payload: {
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) => void;

    onCreatePolygon?: (payload: {
        name: string;
        coordinates: { lat: number; lng: number }[];
        realEstateIds: string[];
    }) => void;
}

/* ------------------------------------------------------------------ */
/* Component                                                          */
/* ------------------------------------------------------------------ */

export default function RealEstateMap({
                                          realEstates,
                                          attachedIds,
                                          center = {lat: 40.114955, lng: -111.654923},
                                          zoom = 11,
                                          containerStyle = {width: "100%", height: "400px"},
                                          displayPolygon,
                                          editablePolygon,
                                          onUpdatePolygon,
                                          onCreatePolygon,
                                      }: RealEstateMapProps) {
    /* -------------------- bootstrap -------------------- */
    const {isLoaded} = useLoadScript({
        googleMapsApiKey: process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || "",
        libraries: LIBRARIES,
    });

    /* -------------------- refs ------------------------- */
    const mapRef = useRef<google.maps.Map | null>(null);
    const drawingMgrRef = useRef<google.maps.drawing.DrawingManager | null>(null);
    const editablePolyRef = useRef<google.maps.Polygon | null>(null);
    const markerPoolRef = useRef<google.maps.Marker[]>([]);

    /* -------------------- state ------------------------ */
    const [mapCenter, setMapCenter] = useState<google.maps.LatLngLiteral>(
        globalCenter || center,
    );
    const [selected, setSelected] = useState<RealEstateMapDto | null>(null);

    const [isDrawing, setIsDrawing] = useState(false);
    const [draftPoly, setDraftPoly] = useState<google.maps.Polygon | null>(null);
    const [draftCoords, setDraftCoords] = useState<
        { lat: number; lng: number }[] | null
    >(null);

    /* ------------------------------------------------------------------ */
    /* Imperative marker pool                                             */
    /* ------------------------------------------------------------------ */
    const rebuildMarkers = useCallback(() => {
        const map = mapRef.current;
        if (!map) return;
        const bounds = map.getBounds();
        if (!bounds) return;

        /* remove old pool */
        markerPoolRef.current.forEach((m) => m.setMap(null));
        markerPoolRef.current = [];

        /* add current visible markers */
        realEstates.forEach((re) => {
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
                    url: attachedIds?.includes(re.id) ? "/yellow-dot.png" : "/red-dot.png",
                },
                title: re.mlsNumber ?? "",
            });
            marker.addListener("click", () => setSelected(re));
            markerPoolRef.current.push(marker);
        });
    }, [realEstates, attachedIds]);

    const rebuildMarkersDebounced = useCallback(debounce(rebuildMarkers, 250), [
        rebuildMarkers,
    ]);

    /* first build after API ready */
    useEffect(() => {
        if (isLoaded && mapRef.current) rebuildMarkers();
    }, [isLoaded, rebuildMarkers]);

    /* ------------------------------------------------------------------ */
    /* Drawing helpers                                                    */
    /* ------------------------------------------------------------------ */
    const getCoords = (path: google.maps.MVCArray<google.maps.LatLng>) =>
        Array.from({length: path.getLength()}, (_, i) => {
            const pt = path.getAt(i);
            return {lat: pt.lat(), lng: pt.lng()};
        });

    const startDrawing = () => {
        draftPoly?.setMap(null);
        setDraftPoly(null);
        setDraftCoords(null);
        setIsDrawing(true);
    };

    const cancelDrawing = () => {
        setIsDrawing(false);
        drawingMgrRef.current?.setDrawingMode(null);
    };

    const handleOverlayComplete = useCallback(
        (e: google.maps.drawing.OverlayCompleteEvent) => {
            if (!isDrawing || e.type !== google.maps.drawing.OverlayType.POLYGON) {
                e.overlay.setMap(null);
                return;
            }

            cancelDrawing();
            const poly = e.overlay as google.maps.Polygon;
            const coords = getCoords(poly.getPath());
            if (coords.length < 3) {
                poly.setMap(null);
                return;
            }

            setDraftPoly(poly);
            setDraftCoords(coords);

            /* keep draftCoords live while editing */
            google.maps.event.addListener(poly.getPath(), "set_at", () =>
                setDraftCoords(getCoords(poly.getPath())),
            );
            google.maps.event.addListener(poly.getPath(), "insert_at", () =>
                setDraftCoords(getCoords(poly.getPath())),
            );
            google.maps.event.addListener(poly.getPath(), "remove_at", () =>
                setDraftCoords(getCoords(poly.getPath())),
            );
        },
        [isDrawing],
    );

    /* ---------------- save new polygon ---------------- */
    const saveDraftPolygon = () => {
        if (!draftPoly || !draftCoords || !onCreatePolygon) return;

        const name = window.prompt("Polygon name:");
        if (!name) return;

        const googlePoly = new google.maps.Polygon({paths: draftCoords});
        const insideIds = realEstates
            .filter(
                (re) =>
                    re.latitude != null &&
                    re.longitude != null &&
                    google.maps.geometry.poly.containsLocation(
                        new google.maps.LatLng(re.latitude, re.longitude),
                        googlePoly,
                    ),
            )
            .map((re) => re.id);

        onCreatePolygon({
            name,
            coordinates: draftCoords,
            realEstateIds: insideIds,
        });

        draftPoly.setMap(null);
        setDraftPoly(null);
        setDraftCoords(null);
    };

    const discardDraftPolygon = () => {
        draftPoly?.setMap(null);
        setDraftPoly(null);
        setDraftCoords(null);
    };

    /* ---------------- save edited polygon ------------- */
    const saveEditablePolygon = () => {
        if (!editablePolygon || !editablePolyRef.current || !onUpdatePolygon) return;

        const coords = getCoords(editablePolyRef.current.getPath());

        const googlePoly = new google.maps.Polygon({paths: coords});
        const insideIds = realEstates
            .filter(
                (re) =>
                    re.latitude != null &&
                    re.longitude != null &&
                    google.maps.geometry.poly.containsLocation(
                        new google.maps.LatLng(re.latitude, re.longitude),
                        googlePoly,
                    ),
            )
            .map((re) => re.id);

        if (!window.confirm("Save polygon changes?")) return;
        onUpdatePolygon({coordinates: coords, realEstateIds: insideIds});
    };

    /* ------------------------------------------------------------------ */
    /* Render                                                             */
    /* ------------------------------------------------------------------ */
    if (!isLoaded) return <div>Loading Map…</div>;

    return (
        <div>
            {/* Drawing toggle */}
            {!editablePolygon && (
                <div className="mb-2">
                    {isDrawing ? (
                        <button onClick={cancelDrawing} className="px-4 py-2 bg-gray-400 rounded">
                            Cancel Drawing
                        </button>
                    ) : (
                        <button onClick={startDrawing} className="px-4 py-2 bg-blue-600 text-white rounded">
                            Draw Polygon
                        </button>
                    )}
                </div>
            )}

            {/* Google Map -------------------------------------------------- */}
            <GoogleMap
                mapContainerStyle={containerStyle}
                center={mapCenter}
                zoom={zoom}
                onLoad={(map) => {
                    mapRef.current = map; // must return void
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
                {/* Drawing manager */}
                <DrawingManager
                    onLoad={(mgr) => (drawingMgrRef.current = mgr)}
                    drawingMode={isDrawing ? google.maps.drawing.OverlayType.POLYGON : null}
                    onOverlayComplete={handleOverlayComplete}
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

                {/* Read-only polygon (blue) */}
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

                {/* Editable polygon (green) */}
                {editablePolygon && (
                    <MapPolygon
                        paths={editablePolygon.coordinates}
                        editable
                        onLoad={(poly) => (editablePolyRef.current = poly)}
                        options={{
                            fillColor: "#00FF00",
                            fillOpacity: 0.3,
                            strokeColor: "#00FF00",
                            strokeWeight: 2,
                        }}
                    />
                )}

                {/* InfoWindow for selected marker */}
                {selected &&
                    selected.latitude != null &&
                    selected.longitude != null && (
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
                                isAttached={attachedIds?.includes(selected.id)}
                                onToggleAttachment={undefined}
                                onClose={() => setSelected(null)}
                                mode={RealEstateMarkerInfoMode.MAP_PAGE}
                            />
                        </InfoWindow>
                    )}
            </GoogleMap>

            {/* Draft polygon actions */}
            {draftPoly && draftCoords && (
                <div className="mt-3 flex gap-2">
                    <button onClick={saveDraftPolygon} className="px-3 py-1 bg-green-600 text-white rounded">
                        Save Polygon
                    </button>
                    <button onClick={discardDraftPolygon} className="px-3 py-1 bg-red-600 text-white rounded">
                        Discard
                    </button>
                </div>
            )}

            {/* Editable polygon save */}
            {editablePolygon && onUpdatePolygon && (
                <div className="mt-3">
                    <button onClick={saveEditablePolygon} className="px-3 py-1 bg-green-600 text-white rounded">
                        Save Changes
                    </button>
                </div>
            )}
        </div>
    );
}
