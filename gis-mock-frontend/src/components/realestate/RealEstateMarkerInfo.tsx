"use client";
import React, { useState } from "react";
import { RealEstate } from "@/interfaces/RealEstate";
import { updateRealEstate } from "@/lib/realEstateApi";
import { RealEstateUpdatePayload } from "@/interfaces/RealEstateUpdatePayload";
import { RealEstateMarkerInfoMode } from "./RealEstateMarkerInfoMode";

interface RealEstateMarkerInfoProps {
    realEstate: RealEstate;
    isAttached?: boolean;
    onToggleAttachment?: () => void;
    onClose?: () => void;
    mode: RealEstateMarkerInfoMode;
}

export default function RealEstateMarkerInfo({
                                                 realEstate,
                                                 isAttached,
                                                 onToggleAttachment,
                                                 onClose,
                                                 mode,
                                             }: RealEstateMarkerInfoProps) {
    const [saving, setSaving] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    // Local states
    const [soldTerms, setSoldTerms] = useState(realEstate.soldTerms || "");
    const [soldPrice, setSoldPrice] = useState(realEstate.soldPrice || "");
    const [mlsNumber, setMlsNumber] = useState(realEstate.mlsNumber || "");
    const [taxId, setTaxId] = useState(realEstate.taxId || "");
    const [address, setAddress] = useState(realEstate.address || "");
    const [city, setCity] = useState(realEstate.city || "");
    const [state, setState] = useState(realEstate.state || "");
    const [zip, setZip] = useState(realEstate.zip || "");
    const [status, setStatus] = useState(realEstate.status || "");

    async function handleSaveChanges() {
        setSaving(true);
        setErrorMessage(null);
        setSuccessMessage(null);

        try {
            const payload: RealEstateUpdatePayload = {
                soldTerms,
                soldPrice,
                mlsNumber,
                taxId,
                address,
                city,
                state,
                zip,
                status,
            };
            await updateRealEstate(realEstate.id, payload);
            setSuccessMessage("Real estate updated successfully!");
        } catch (err: any) {
            setErrorMessage(err.message || "Failed to update real estate.");
        } finally {
            setSaving(false);
        }
    }

    function handleToggleAttachment() {
        if (!onToggleAttachment) return;
        onToggleAttachment();
        if (mode === RealEstateMarkerInfoMode.POLYGON_EDIT_PAGE && onClose) {
            onClose();
        }
    }

    return (
        <div style={{ minWidth: 220 }}>
            {mode === RealEstateMarkerInfoMode.POLYGON_EDIT_PAGE && (
                <div className="mb-2">
                    <button
                        onClick={handleToggleAttachment}
                        className="px-2 py-1 bg-blue-500 text-white rounded text-xs"
                    >
                        {isAttached ? "Remove from Polygon" : "Attach to Polygon"}
                    </button>
                </div>
            )}

            <div className="flex flex-col space-y-1 text-sm">
                <label>
                    <span className="font-semibold text-xs">MLS#:</span>
                    <input
                        type="text"
                        className="border w-full p-1 text-sm"
                        value={mlsNumber}
                        onChange={(e) => setMlsNumber(e.target.value)}
                    />
                </label>
                <label>
                    <span className="font-semibold text-xs">Tax ID:</span>
                    <input
                        type="text"
                        className="border w-full p-1 text-sm"
                        value={taxId}
                        onChange={(e) => setTaxId(e.target.value)}
                    />
                </label>
                <label>
                    <span className="font-semibold text-xs">Sold Price:</span>
                    <input
                        type="text"
                        className="border w-full p-1 text-sm"
                        value={soldPrice}
                        onChange={(e) => setSoldPrice(e.target.value)}
                    />
                </label>
                <label>
                    <span className="font-semibold text-xs">Sold Terms:</span>
                    <input
                        type="text"
                        className="border w-full p-1 text-sm"
                        value={soldTerms}
                        onChange={(e) => setSoldTerms(e.target.value)}
                    />
                </label>
                <label>
                    <span className="font-semibold text-xs">Address:</span>
                    <input
                        type="text"
                        className="border w-full p-1 text-sm"
                        value={address}
                        onChange={(e) => setAddress(e.target.value)}
                    />
                </label>
                <label>
                    <span className="font-semibold text-xs">City:</span>
                    <input
                        type="text"
                        className="border w-full p-1 text-sm"
                        value={city}
                        onChange={(e) => setCity(e.target.value)}
                    />
                </label>
                <label>
                    <span className="font-semibold text-xs">State:</span>
                    <input
                        type="text"
                        className="border w-full p-1 text-sm"
                        value={state}
                        onChange={(e) => setState(e.target.value)}
                    />
                </label>
                <label>
                    <span className="font-semibold text-xs">Zip:</span>
                    <input
                        type="text"
                        className="border w-full p-1 text-sm"
                        value={zip}
                        onChange={(e) => setZip(e.target.value)}
                    />
                </label>
                <label>
                    <span className="font-semibold text-xs">Status:</span>
                    <input
                        type="text"
                        className="border w-full p-1 text-sm"
                        value={status}
                        onChange={(e) => setStatus(e.target.value)}
                    />
                </label>
            </div>

            {successMessage && (
                <div className="bg-green-100 text-green-800 p-2 mb-2 text-xs rounded">
                    {successMessage}
                </div>
            )}
            {errorMessage && (
                <div className="bg-red-100 text-red-800 p-2 mb-2 text-xs rounded">
                    {errorMessage}
                </div>
            )}

            <div className="flex justify-between items-center mt-3">
                <button
                    onClick={handleSaveChanges}
                    disabled={saving}
                    className="px-2 py-1 bg-green-600 text-white rounded text-xs"
                >
                    {saving ? "Saving..." : "Save Changes"}
                </button>
                <button
                    onClick={onClose}
                    className="px-2 py-1 bg-gray-300 text-xs rounded"
                >
                    Close
                </button>
            </div>
        </div>
    );
}
