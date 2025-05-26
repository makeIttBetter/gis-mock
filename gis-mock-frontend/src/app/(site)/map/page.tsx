"use client";

import React, {Suspense} from "react";
import RealEstateDashboardContainer from "@/components/dashboard/RealEstateDashboardContainer";

export default function MapPage() {
    return (
        <Suspense fallback={<div>Loading map...</div>}>
            <RealEstateDashboardContainer/>
        </Suspense>
    );
}
