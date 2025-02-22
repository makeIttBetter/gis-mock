"use client";
import React, {useState} from "react";
import {RealEstate} from "@/interfaces/RealEstate";
import {updateRealEstate} from "@/lib/realEstateApi";
import {RealEstateUpdatePayload} from "@/interfaces/RealEstateUpdatePayload";
import {RealEstateMarkerInfoMode} from "./RealEstateMarkerInfoMode";

interface RealEstateMarkerInfoProps {
    /** The RealEstate object we are editing. */
    realEstate: RealEstate;

    /**
     * If in polygon-edit context, indicates whether this RE is already
     * attached to the polygon or not.
     */
    isAttached?: boolean;

    /**
     * If the user wants to attach/remove the real estate from the polygon,
     * the parent can handle that. (Only relevant in POLYGON_EDIT_PAGE mode.)
     */
    onToggleAttachment?: () => void;

    /** Called after we finish saving or if user closes the pop-up. */
    onClose?: () => void;

    /**
     * MAP_PAGE or POLYGON_EDIT_PAGE.
     * - If POLYGON_EDIT_PAGE, we show the attach/remove button
     *   and auto-close after toggling.
     */
    mode: RealEstateMarkerInfoMode;
}

/**
 * Displays a small editing form with the fields:
 * - Sold Terms
 * - Sold Price
 * - MLS#
 * - Tax ID
 * - Address
 * - City
 * - State
 * - Zip
 * - Status
 *
 * The user can save changes, see a success/fail message, or attach/remove polygon if in polygon-edit.
 */
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

    // Keep local state for the editable fields
    const [soldTerms, setSoldTerms] = useState(realEstate.soldTerms || "");
    const [soldPrice, setSoldPrice] = useState(realEstate.soldPrice || "");
    const [mlsNumber, setMlsNumber] = useState(realEstate.mlsNumber || "");
    const [taxId, setTaxId] = useState(realEstate.taxId || "");
    const [address, setAddress] = useState(realEstate.address || "");
    const [city, setCity] = useState(realEstate.city || "");
    const [state, setState] = useState(realEstate.state || "");
    const [zip, setZip] = useState(realEstate.zip || "");
    const [status, setStatus] = useState(realEstate.status || "");

    // Handler for "Save Changes"
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

            // Show success message
            setSuccessMessage("Real estate updated successfully!");

            // Optionally, we could call onClose() if we want to auto-close after success.
            // But the requirement says show a small pop-up with success. We'll let the user decide to close.
        } catch (err: any) {
            setErrorMessage(err.message || "Failed to update Real Estate.");
        } finally {
            setSaving(false);
        }
    }

    // Handler for "Attach to Polygon" or "Remove from Polygon"
    function handleToggleAttachment() {
        if (!onToggleAttachment) return;

        // Just call the parent's function
        onToggleAttachment();

        // If in polygon-edit mode, auto-close after toggling
        if (mode === RealEstateMarkerInfoMode.POLYGON_EDIT_PAGE && onClose) {
            onClose();
        }
    }

    return (
        <div style={{minWidth: 220}}>
            {/* If in polygon-edit mode, show attach/remove button at top-left */}
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

            {/* Editable form fields */}
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
                <a>--</a>
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
                <a>--</a>
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
                <a>--</a>
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

            {/* Display success or error messages if any */}
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

            {/* Buttons row */}
            <div className="flex justify-between items-center mt-3">
                {/* Save Changes */}
                <button
                    onClick={handleSaveChanges}
                    disabled={saving}
                    className="px-2 py-1 bg-green-600 text-white rounded text-xs"
                >
                    {saving ? "Saving..." : "Save Changes"}
                </button>

                {/* Close popup */}
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
