"use client";

import React, {useEffect, useState} from "react";
import {fetchProfile, updateProfile} from "@/lib/profileApi";
import {ProfileResponseDto} from "@/interfaces/ProfileResponseDto";
import {ProfileRequestDto} from "@/interfaces/ProfileRequestDto";

export default function ProfilePage() {

    // States for profile data
    const [, setCurrentUsername] = useState<string>("");
    const [loadingProfile, setLoadingProfile] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // Fields that can be changed
    const [username, setUsername] = useState<string>("");
    const [oldPassword, setOldPassword] = useState<string>("");
    const [newPassword, setNewPassword] = useState<string>("");
    const [confirmNewPassword, setConfirmNewPassword] = useState<string>("");

    // Success message state
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [updating, setUpdating] = useState<boolean>(false);

    // On mount, load the current profile
    useEffect(() => {
        setLoadingProfile(true);
        fetchProfile()
            .then((res: ProfileResponseDto) => {
                setCurrentUsername(res.username);
                setUsername(res.username);
            })
            .catch((err) => {
                console.error("Failed to load profile", err);
                setError(err.message || "Failed to load profile");
            })
            .finally(() => {
                setLoadingProfile(false);
            });
    }, []);

    // Handle form submission
    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError(null);
        setSuccessMessage(null);
        setUpdating(true);

        // Build the request
        const payload: ProfileRequestDto = {
            username: username.trim(),
        };

        // If newPassword is not empty, user wants to change password
        if (newPassword.trim()) {
            payload.oldPassword = oldPassword;
            payload.newPassword = newPassword;
            payload.confirmNewPassword = confirmNewPassword;
        }

        try {
            const result = await updateProfile(payload);
            setSuccessMessage("Profile updated successfully!");
            // Also update the current username shown
            setCurrentUsername(result.username);
        } catch (err: any) {
            console.error("Failed to update profile", err);
            setError(err.message);
        } finally {
            setUpdating(false);
        }
    }

    if (loadingProfile) {
        return (
            <div className="flex justify-center items-center min-h-screen">
                <p>Loading profile...</p>
            </div>
        );
    }

    return (
        <div className="p-4 max-w-md mx-auto bg-white rounded shadow mt-8">
            <h1 className="text-2xl font-bold mb-4">Your Profile</h1>

            {error && <div className="text-red-600 mb-3">{error}</div>}
            {successMessage && (
                <div className="text-green-600 mb-3">{successMessage}</div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
                {/* Current Username (text field) */}
                <div>
                    <label className="block text-sm font-semibold">Username</label>
                    <input
                        type="text"
                        className="border rounded p-2 w-full"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        disabled={updating}
                    />
                </div>

                {/* Old Password (only needed if user is changing password) */}
                <div>
                    <label className="block text-sm font-semibold">Old Password (required if changing password)</label>
                    <input
                        type="password"
                        className="border rounded p-2 w-full"
                        value={oldPassword}
                        onChange={(e) => setOldPassword(e.target.value)}
                        disabled={updating}
                    />
                </div>

                {/* New Password */}
                <div>
                    <label className="block text-sm font-semibold">New Password</label>
                    <input
                        type="password"
                        className="border rounded p-2 w-full"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        disabled={updating}
                    />
                </div>

                {/* Confirm New Password */}
                <div>
                    <label className="block text-sm font-semibold">Confirm New Password</label>
                    <input
                        type="password"
                        className="border rounded p-2 w-full"
                        value={confirmNewPassword}
                        onChange={(e) => setConfirmNewPassword(e.target.value)}
                        disabled={updating}
                    />
                </div>

                {/* Submit */}
                <button
                    type="submit"
                    className="bg-blue-600 text-white px-4 py-2 rounded"
                    disabled={updating}
                >
                    {updating ? "Updating..." : "Update Profile"}
                </button>
            </form>
        </div>
    );
}
